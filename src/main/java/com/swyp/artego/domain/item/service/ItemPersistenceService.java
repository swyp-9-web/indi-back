package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.request.ItemUpdateRequest;
import com.swyp.artego.domain.item.dto.response.ItemCreateResponse;
import com.swyp.artego.domain.item.dto.response.ItemUpdateResponse;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.item.service.utils.SizeTypeUtils;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.event.ImageDeleteEvent;
import com.swyp.artego.global.file.event.UploadRollbackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemPersistenceService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SizeTypeUtils sizeTypeUtils;

    /**
     * 작품 조회 및 유효성 검사
     */
    @Transactional(readOnly = true)
    public Item loadAndValidateItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        return item;
    }

    @Transactional
    public ItemCreateResponse saveItemWithTransaction(User user, ItemCreateRequest request, List<String> imgUrls) {

        SizeType sizeType = sizeTypeUtils.calculateSizeType(
                request.getSize().getWidth(),
                request.getSize().getHeight(),
                request.getSize().getDepth()
        );

        userRepository.incrementItemCount(user.getId(), 1);

        return ItemCreateResponse.fromEntity(
                itemRepository.save(request.toEntity(user, imgUrls, sizeType))
        );
    }

    @Transactional
    public ItemUpdateResponse updateItemWithTransaction(ItemUpdateRequest request, Item item, List<String> updateImgUrls) {
        List<String> previousImgUrls = item.getImgUrls();

        // 롤백 이벤트 등록. DB 롤백 시 S3에 새로 업로드했던 이미지를 삭제한다.
        List<String> newImgUrls = updateImgUrls.stream()
                .filter(url -> !previousImgUrls.contains(url))
                .toList();
        applicationEventPublisher.publishEvent(new UploadRollbackEvent(newImgUrls));

        // 커밋 이벤트 등록. DB 커밋 시 더 이상 사용하지 않는 이미지를 삭제한다.
        if (!previousImgUrls.equals(updateImgUrls)) {
            List<String> deletedImgUrls = previousImgUrls.stream()
                    .filter(url -> !updateImgUrls.contains(url))
                    .toList();

            log.debug("[updateItemWithTransaction] 더 이상 사용하지 않는 작품 이미지를 삭제합니다.");
            applicationEventPublisher.publishEvent(new ImageDeleteEvent(deletedImgUrls));
        }

        ItemCreateRequest.ItemSize requestSize = request.getSize();
        SizeType sizeType = sizeTypeUtils.calculateSizeType(requestSize.getWidth(), requestSize.getHeight(), requestSize.getDepth());
        request.applyToEntity(item, updateImgUrls, sizeType);

        return ItemUpdateResponse.fromEntity(item);
    }
}
