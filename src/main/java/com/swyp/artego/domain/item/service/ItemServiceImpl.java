package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.request.ItemUpdateRequest;
import com.swyp.artego.domain.item.dto.response.*;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.scrap.repository.ScrapRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.domain.user.repository.UserRepository;
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

    private final FileService fileService;

    @Value("${ncp.storage.bucket.folder.item-post}")
    private String folderName;

    @Override
    @Transactional
    public ItemCreateResponse createItem(AuthUser authUser, ItemCreateRequest request, List<MultipartFile> multipartFiles) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (user.getRole() != Role.ARTIST) {
            throw new BusinessExceptionHandler("작품을 등록할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        List<String> imageOrder = request.getImageOrder();
        validateFileSizeAndNameMatch(multipartFiles, imageOrder);
        List<String> imgUrls = fileService.uploadNewFilesInOrder(multipartFiles, imageOrder, folderName);

        ItemCreateRequest.ItemSize requestSize = request.getSize();
        SizeType sizeType = calculateSizeType(requestSize.getWidth(), requestSize.getHeight(), requestSize.getDepth());

        return ItemCreateResponse.fromEntity(
                itemRepository.save(request.toEntity(user, imgUrls, sizeType))
        );

    }

    @Override
    @Transactional(readOnly = true)
    public ItemFindByItemIdResponse findItemByItemId(AuthUser authUser, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Boolean isScrapped = null;
        boolean isOwner = false;
        if (authUser != null) {
            User user = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

            isScrapped = scrapRepository.existsByUserIdAndItemId(user.getId(), item.getId());

            if (Objects.equals(item.getUser().getId(), user.getId())) {
                isOwner = true;
            }
        }

        return ItemFindByItemIdResponse.fromEntity(item, isScrapped, isOwner);
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
    @Transactional
    public ItemUpdateResponse updateItem(AuthUser authUser, Long itemId, ItemUpdateRequest request, List<MultipartFile> multipartFiles) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("작품을 수정할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        // 이미지 요소 검증 및 imgUrls 반환, 사용하지 않는 사진은 삭제
        List<String> imageOrder = request.getImageOrder();
        validateFileSizeAndNameMatch(multipartFiles, imageOrder);

        List<String> updatedImageUrls = fileService.uploadNewFilesInOrder(multipartFiles, imageOrder, folderName);

        deleteRemovedImages(item.getImgUrls(), updatedImageUrls);

        // 이미지 외 나머지 요소 수정
        ItemCreateRequest.ItemSize requestSize = request.getSize();
        SizeType sizeType = calculateSizeType(requestSize.getWidth(), requestSize.getHeight(), requestSize.getDepth());
        request.applyToEntity(item, updatedImageUrls, sizeType);

        return ItemUpdateResponse.fromEntity(item);
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

        return ItemDeleteResponse.fromEntity(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request) {
        return itemRepository.searchItems(authUser, request);
    }

    /**
     * 제품의 가로, 세로, 높이를 토대로 제품의 사이즈(S,M,L 혹은 X)를 구한다.
     *
     * @param width  가로
     * @param height 세로
     * @param depth  폭
     * @return SizeType S, M, L 사이즈. X는 실측이 불가능한 작품을 의미합니다.
     */
    private SizeType calculateSizeType(int width, int height, int depth) {
        int sum = width + height + depth;

        if (sum == 0) return SizeType.X;
        else if (sum <= 100) return SizeType.S;
        else if (sum <= 160) return SizeType.M;
        else return SizeType.L;
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

    /**
     * [수정 API] 삭제할 이미지 추출, S3에서 삭제 실행
     *
     * @param originalUrls   기존 게시글의 이미지 URLs
     * @param updatedImgUrls 수정한 결과 이미지 Urls
     */
    private void deleteRemovedImages(List<String> originalUrls, List<String> updatedImgUrls) {

        List<String> deletedUrls = originalUrls.stream()
                .filter(url -> !updatedImgUrls.contains(url))
                .toList();

        List<String> deletedKeys = deletedUrls.stream()
                .map(fileService::extractKeyFromImgUrl)
                .toList();

        if (!deletedKeys.isEmpty()) {
            log.info("[ItemService] 수정 API: #### 게시글에서 내린 사진을 S3에서 삭제 ####");
            fileService.deleteFiles(deletedKeys);
        }
    }
}
