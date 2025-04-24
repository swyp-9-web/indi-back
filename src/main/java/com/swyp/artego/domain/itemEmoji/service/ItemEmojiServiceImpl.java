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
    public void createItemEmoji(AuthUser authUser, ItemEmojiCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessExceptionHandler("아이템이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        ItemEmoji itemEmoji = ItemEmoji.builder()
                .user(user)
                .item(item)
                .emojiType(request.getEmojiType())
                .build();

        itemEmojiRepository.save(itemEmoji);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemEmojiInfoResponse> getAllItemEmojis() {
        return itemEmojiRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemEmojiInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }


}
