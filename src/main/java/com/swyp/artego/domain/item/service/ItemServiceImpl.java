package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.follow.repository.FollowRepository;
import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.request.ItemUpdateRequest;
import com.swyp.artego.domain.item.dto.response.*;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.itemEmoji.entity.ItemEmoji;
import com.swyp.artego.domain.itemEmoji.repository.ItemEmojiRepository;
import com.swyp.artego.domain.scrap.repository.ScrapRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.domain.user.service.UserPersistenceService;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final FollowRepository followRepository;
    private final ItemEmojiRepository itemEmojiRepository;

    private final ItemPersistenceService itemPersistenceService;
    private final UserPersistenceService userPersistenceService;
    private final FileService fileService;

    @Value("${ncp.storage.bucket.folder.item-post}")
    private String folderName;

    @Override
    public ItemCreateResponse createItem(AuthUser authUser, ItemCreateRequest request, List<MultipartFile> multipartFiles) {
        User user = userPersistenceService.loadAndValidateUser(authUser.getOauthId());
        if (user.getRole() != Role.ARTIST) {
            throw new BusinessExceptionHandler("작품을 등록할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        List<String> imageOrder = request.getImageOrder();
        validateFileSizeAndNameMatch(multipartFiles, imageOrder);
        List<String> imgUrls = fileService.uploadNewFilesInOrder(multipartFiles, imageOrder, folderName);

        return itemPersistenceService.saveItemWithTransaction(user, request, imgUrls);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemFindByItemIdResponse findItemByItemId(AuthUser authUser, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        // 상태가 HIDE인 경우 예외 발생
        if (item.getStatusType() == StatusType.HIDE) {
            throw new BusinessExceptionHandler("숨김 처리된 작품입니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        Long totalScrapCount = scrapRepository.countAllByItemId(item.getId());

        boolean isScrapped = false;
        boolean isFollowing = false;
        boolean isOwner = false;

        // 이모지 관련
        boolean isLiked = false;
        boolean isWanted = false;
        boolean isRevisited = false;

        Long likedEmojiId = null;
        Long wantedEmojiId = null;
        Long revisitedEmojiId = null;

        if (authUser != null) {
            User user = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

            Long userId = user.getId();
            Long artistId = item.getUser().getId();

            isScrapped = scrapRepository.existsByUserIdAndItemId(userId, itemId);
            isFollowing = followRepository.existsByUserIdAndUserArtistId(userId, artistId);
            isOwner = userId.equals(artistId);


            List<ItemEmoji> userEmojis = itemEmojiRepository.findAllByUserIdAndItemId(userId, itemId);
            for (ItemEmoji emoji : userEmojis) {
                switch (emoji.getEmojiType()) {
                    case LIKES -> {
                        isLiked = true;
                        likedEmojiId = emoji.getId();
                    }
                    case WANTS -> {
                        isWanted = true;
                        wantedEmojiId = emoji.getId();
                    }
                    case REVISITS -> {
                        isRevisited = true;
                        revisitedEmojiId = emoji.getId();
                    }
                }
            }
        }

        return ItemFindByItemIdResponse.fromEntity(
                item,
                totalScrapCount,
                isScrapped,
                isFollowing,
                isOwner,
                isLiked,
                isWanted,
                isRevisited,
                likedEmojiId,
                wantedEmojiId,
                revisitedEmojiId
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<ItemInfoResponse> getAllItems() {
        return itemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ItemUpdateResponse updateItem(AuthUser authUser, Long itemId, ItemUpdateRequest request, List<MultipartFile> multipartFiles) {
        Item item = itemPersistenceService.loadAndValidateItem(itemId);

        User user = userPersistenceService.loadAndValidateUser(authUser.getOauthId());
        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("작품을 수정할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        // S3 호출
        List<String> imageOrder = request.getImageOrder();
        List<String> updateImgUrls;
        if (multipartFiles != null) {
            validateFileSizeAndNameMatch(multipartFiles, imageOrder);
            updateImgUrls = fileService.uploadNewFilesInOrder(multipartFiles, imageOrder, folderName);
        } else {
            updateImgUrls = imageOrder;
        }

        // DB 업데이트
        return itemPersistenceService.updateItemWithTransaction(request, item, updateImgUrls);
    }

    @Override
    @Transactional
    public ItemDeleteResponse deleteItem(AuthUser authUser, Long itemId) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("작품을 삭제할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        item.setStatusType(StatusType.HIDE);

        userRepository.incrementItemCount(item.getUser().getId(), -1);


        // 3. 작가의 scrapCount, reactionCount 감소

        Long artistId = item.getUser().getId();
        userRepository.incrementUserScrapCount(artistId, -item.getScrapCount());
        userRepository.incrementUserReactionCount(artistId, -item.getTotalReactionCount());

        return ItemDeleteResponse.fromEntity(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request) {
        return itemRepository.searchItems(authUser, request);
    }

    /**
     * [등록, 수정 API] 파일 이름 및 개수 유효성 검증
     * <p>
     * imageOrder로부터 새로 추가될 이미지 리스트와 multipartFiles로부터 새로 추가될 이미지의 실제 이름 리스트를 비교한다.
     * 두 리스트의 사이즈를 비교하고 실제 이름을 비교한다.
     *
     * @param multipartFiles
     * @param imageOrder     사용자가 새로 정렬한 사진 순서
     */
    private static void validateFileSizeAndNameMatch(List<MultipartFile> multipartFiles, List<String> imageOrder) {

        List<String> expectedNewImageNames = imageOrder.stream()
                .filter(name -> !name.startsWith("https"))
                .toList();

        List<String> multipartFileNames = Optional.ofNullable(multipartFiles)
                .orElse(Collections.emptyList())
                .stream()
                .map(MultipartFile::getOriginalFilename)
                .toList();

        Set<String> expectedSet = new HashSet<>(expectedNewImageNames);
        Set<String> actualSet = new HashSet<>(multipartFileNames);

        if (!expectedSet.equals(actualSet)) {
            throw new BusinessExceptionHandler(
                    "imageOrder에 명시된 새 이미지와 업로드된 파일 이름 혹은 개수가 일치하지 않습니다. " +
                            "누락되었거나 불필요한 파일이 존재할 수 있습니다.",
                    ErrorCode.BAD_REQUEST_ERROR
            );
        }
    }
}
