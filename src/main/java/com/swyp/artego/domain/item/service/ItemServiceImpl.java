package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.request.ItemUpdateRequest;
import com.swyp.artego.domain.item.dto.response.*;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.dto.response.FileUploadResponseExample;
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final FileService fileService;

    @Override
    @Transactional
    public ItemCreateResponse createItem(AuthUser authUser, ItemCreateRequest request, List<MultipartFile> multipartFiles, String folderName) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        List<String> imgUrls = fileService.uploadFiles(multipartFiles, folderName);

        ItemCreateRequest.ItemSize requestSize = request.getSize();
        SizeType sizeType = calculateSizeType(requestSize.getWidth(), requestSize.getHeight(), requestSize.getDepth());

        return ItemCreateResponse.fromEntity(
                itemRepository.save(request.toEntity(user, imgUrls, sizeType))
        );

    }

    @Override
    @Transactional
    public ItemUpdateResponse updateItem(AuthUser authUser, Long itemId, ItemUpdateRequest request, List<MultipartFile> multipartFiles, String folderName) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessExceptionHandler("작품을 수정할 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        List<String> orderedList = request.getOrderedImageList();

        // 1. 새로 추가될 이미지 이름 리스트
        List<String> expectedNewImageNames = orderedList.stream()
                .filter(name -> !name.startsWith("https"))
                .toList();

        // 2. 파일 이름 리스트
        List<String> actualUploadedFileNames = Optional.ofNullable(multipartFiles)
                .orElse(Collections.emptyList())
                .stream()
                .map(MultipartFile::getOriginalFilename)
                .toList();

        // 3. 개수 비교
        if (expectedNewImageNames.size() != actualUploadedFileNames.size()) {
            throw new BusinessExceptionHandler("새 이미지 개수와 업로드된 파일 개수가 일치하지 않습니다.", ErrorCode.BAD_REQUEST_ERROR);

        }

        // 4. 파일 이름 일치 여부 확인 (순서 상관 있는 버전)
        for (int i = 0; i < expectedNewImageNames.size(); i++) {
            if (!expectedNewImageNames.get(i).equals(actualUploadedFileNames.get(i))) {
                throw new BusinessExceptionHandler("파일 이름 불일치: orderedList에 있는 사진 이름은 " +
                        expectedNewImageNames.get(i) + " 이지만, multipartfile에는 " + actualUploadedFileNames.get(i) + " 이 전송되었습니다.", ErrorCode.BAD_REQUEST_ERROR);
            }
        }

        // 이미지 수정 처리 TODO: fileService 로 분리?
        List<String> finalImageUrls = new ArrayList<>();
        Iterator<MultipartFile> fileIterator = Optional.ofNullable(multipartFiles).orElse(Collections.emptyList()).iterator();
        List<String> uploadedKeys = new ArrayList<>();

        for (String imageRef : orderedList) {
            if (imageRef.startsWith("https")) {
                finalImageUrls.add(imageRef);
            } else {
                MultipartFile file = fileIterator.next();
                FileUploadResponseExample response;
                try {
                    response = fileService.uploadFile(file, folderName);
                    uploadedKeys.add(response.getUploadKey());
                } catch (BusinessExceptionHandler e) {
                    log.info("[ItemService] 수정 API: #### 업로드된 파일 롤백 시작 ####");
                    fileService.rollbackUploadedFiles(uploadedKeys);
                    throw e; // 단일 업로드에서도 이미 비즈니스 예외로 변환했기 때문에 그대로 던짐
                }
                finalImageUrls.add(response.getUploadFileUrl());
            }
        }

        List<String> originalUrls = item.getImgUrls(); // 기존 이미지 URL
        List<String> urlsToKeep = finalImageUrls;     // 최종 유지할 이미지 URL

        // 삭제할 URL만 추출
        List<String> deletedUrls = originalUrls.stream()
                .filter(url -> !urlsToKeep.contains(url))
                .toList();

        // URL -> key 변환 로직 (예: https://s3.com/folder/key.jpg → key.jpg)
        List<String> deletedKeys = deletedUrls.stream()
                .map(fileService::extractKeyFromImgUrl) // 또는 직접 key 추출 로직
                .toList();

        // 삭제 실행
        if (!deletedKeys.isEmpty()) {
            log.info("[ItemService] 수정 API: #### 사용자가 삭제한 사진을 S3에서 삭제 ####");
            fileService.rollbackUploadedFiles(deletedKeys);
        }

        // 이미지 외 나머지 요소 수정
        ItemCreateRequest.ItemSize requestSize = request.getSize();
        SizeType sizeType = calculateSizeType(requestSize.getWidth(), requestSize.getHeight(), requestSize.getDepth());
        request.applyToEntity(item, finalImageUrls, sizeType);

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
    public List<ItemInfoResponse> getAllItems() {
        return itemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemInfoResponse::fromEntity)
                .collect(Collectors.toList());
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


    @Override
    @Transactional(readOnly = true)
    public ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request) {
        return itemRepository.searchItems(authUser, request);
    }


}
