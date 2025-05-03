package com.swyp.artego.domain.itemEmoji.service;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.itemEmoji.dto.request.ItemEmojiCreateRequest;
import com.swyp.artego.domain.itemEmoji.entity.ItemEmoji;
import com.swyp.artego.domain.itemEmoji.dto.response.ItemEmojiInfoResponse;
import com.swyp.artego.domain.itemEmoji.repository.ItemEmojiRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;

import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemEmojiServiceImpl implements ItemEmojiService {

    private final ItemEmojiRepository itemEmojiRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Long createItemEmoji(AuthUser authUser, ItemEmojiCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessExceptionHandler("아이템이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        boolean exists = itemEmojiRepository.findByUserAndItemAndEmojiType(user, item, request.getEmojiType()).isPresent();
        if (exists) {
            throw new BusinessExceptionHandler("이미 이모지를 추가했습니다.", ErrorCode.DUPLICATE_RESOURCE);
        }

        ItemEmoji itemEmoji = ItemEmoji.builder()
                .user(user)
                .item(item)
                .emojiType(request.getEmojiType())
                .build();

        ItemEmoji saved = itemEmojiRepository.save(itemEmoji);
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemEmojiInfoResponse> getAllItemEmojis() {
        return itemEmojiRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemEmojiInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void deleteItemEmojiById(Long itemEmojiId) {
        ItemEmoji emoji = itemEmojiRepository.findById(itemEmojiId)
                .orElseThrow(() -> new BusinessExceptionHandler("이모지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_ERROR));

        itemEmojiRepository.delete(emoji);
    }



}
