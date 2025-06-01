package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;
import com.swyp.artego.domain.item.dto.response.ItemCreateResponse;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.item.service.utils.SizeTypeUtils;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemPersistenceService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final SizeTypeUtils sizeTypeUtils;

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
}
