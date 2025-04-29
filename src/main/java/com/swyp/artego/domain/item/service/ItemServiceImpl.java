package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;

import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.response.ItemInfoResponse;
import com.swyp.artego.domain.item.dto.response.ItemSearchResponse;
import com.swyp.artego.domain.item.dto.response.ItemSearchResultResponse;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.repository.ItemRepository;
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
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;



    @Override
    @Transactional
    public void createItem(AuthUser authUser, ItemCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        SizeType sizeType = calculateSizeType(request.getSizeWidth(), request.getSizeLength(), request.getSizeHeight());

        itemRepository.save(request.toEntity(user, sizeType));

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
     * 제품의 가로, 세로, 높이를 토대로 제품의 사이즈(S,M,L)를 구한다.
     * TODO: 사이즈 분류 기준 기획 나오면 로직 구현. 일단은 모두 M사이즈로 반환한다.
     *
     * @param width 가로
     * @param length 세로
     * @param height 높이
     * @return SizeType
     */
    private SizeType calculateSizeType(int width, int length, int height) {

        return SizeType.M;
    }


    @Override
    @Transactional(readOnly = true)
    public ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request) {
        return itemRepository.searchItems(authUser, request);
    }


}
