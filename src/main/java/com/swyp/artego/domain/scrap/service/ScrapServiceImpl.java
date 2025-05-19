package com.swyp.artego.domain.scrap.service;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.repository.ItemRepository;
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

@Service
@RequiredArgsConstructor
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void createScrap(AuthUser authUser, Long itemId) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 유저입니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("해당 아이템이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        boolean alreadyScrapped = scrapRepository.findByUserAndItem(user, item).isPresent();
        if (alreadyScrapped) {
            throw new BusinessExceptionHandler("이미 스크랩한 아이템입니다.", ErrorCode.DUPLICATE_RESOURCE);
        }

        Scrap scrap = Scrap.builder()
                .user(user)
                .item(item)
                .build();

        scrapRepository.save(scrap);

        itemRepository.incrementScrapCount(item.getId(), 1); // count
        userRepository.incrementUserScrapCount(item.getUser().getId(), 1); //count
    }

    @Override
    @Transactional
    public void deleteScrap(AuthUser authUser, Long itemId) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("존재하지 않는 유저입니다.", ErrorCode.NOT_FOUND_ERROR));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("해당 아이템이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Scrap scrap = scrapRepository.findByUserAndItem(user, item)
                .orElseThrow(() -> new BusinessExceptionHandler("스크랩 정보가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        scrapRepository.delete(scrap);

        scrapRepository.delete(scrap);
        itemRepository.incrementScrapCount(item.getId(), -1);
        userRepository.incrementUserScrapCount(item.getUser().getId(), -1);

    }
}
