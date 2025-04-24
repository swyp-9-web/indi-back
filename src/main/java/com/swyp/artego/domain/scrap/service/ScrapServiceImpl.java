package com.swyp.artego.domain.scrap.service;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.scrap.dto.response.ScrapInfoResponse;
import com.swyp.artego.domain.scrap.entity.Scrap;
import com.swyp.artego.domain.scrap.repository.ScrapRepository;
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
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    @Transactional
    public void createScrap(AuthUser authUser, Long itemId) {

        // 유저 조회 - 비즈니스 예외 처리
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler(
                        "존재하지 않는 유저입니다. oauthId: " + authUser.getOauthId(),
                        ErrorCode.NOT_FOUND_ERROR));

        // 아이템 조회 - 비즈니스 예외 처리
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler(
                        "해당 아이템이 존재하지 않습니다. itemId: " + itemId,
                        ErrorCode.NOT_FOUND_ERROR));

        Scrap scrap = Scrap.builder()
                .user(user)
                .item(item)
                .build();

        scrapRepository.save(scrap);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ScrapInfoResponse> getAllScraps() {
        return scrapRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ScrapInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }


}
