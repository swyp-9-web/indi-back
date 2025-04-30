package com.swyp.artego.domain.item.service;

import com.swyp.artego.domain.item.dto.request.ItemCreateRequest;


import com.swyp.artego.domain.item.dto.request.ItemSearchRequest;
import com.swyp.artego.domain.item.dto.response.ItemCreateResponse;
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
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param width 가로
     * @param height 세로
     * @param depth 폭
     * @return SizeType S, M, L 사이즈. X는 실측이 불가능한 작품을 의미합니다.
     */
    private SizeType calculateSizeType(int width, int height, int depth) {
        int sum = width + height + depth;

        if (sum == 0)   return SizeType.X;
        else if (sum <= 100)    return SizeType.S;
        else if (sum <= 160)    return SizeType.M;
        else return SizeType.L;
    }


    @Override
    @Transactional(readOnly = true)
    public ItemSearchResultResponse searchItems(AuthUser authUser, ItemSearchRequest request) {
        return itemRepository.searchItems(authUser, request);
    }


}
