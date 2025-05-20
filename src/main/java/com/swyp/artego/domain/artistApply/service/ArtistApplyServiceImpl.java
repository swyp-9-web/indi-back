package com.swyp.artego.domain.artistApply.service;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.request.ConvertToArtistRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.domain.artistApply.entity.ArtistApply;
import com.swyp.artego.domain.artistApply.repository.ArtistApplyRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistApplyServiceImpl implements ArtistApplyService {

    private final ArtistApplyRepository artistApplyRepository;

    private final UserRepository userRepository;

    @Transactional
    public ArtistApplyCreateResponse createArtistApply(AuthUser authUser, ArtistApplyCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        ArtistApply apply = new ArtistApply(
                user,
                request.getArtistAboutMe(),
                request.getEmail(),
                request.getSnsLink()
        );

        ArtistApply saved = artistApplyRepository.save(apply);

        return new ArtistApplyCreateResponse(saved.getId());




    }

    @Transactional
    @Override
    public void convertUserToArtist(AuthUser adminUser, ConvertToArtistRequest request) {
        User admin = userRepository.findByOauthId(adminUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("관리자 계정이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        // 관리자 권한 체크
        if (!admin.getRole().equals(Role.ADMIN)) {
            throw new BusinessExceptionHandler("접근 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        // 대상 유저 조회
        User target = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessExceptionHandler("대상 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_ERROR));

        // 작가 권한 부여
        target.setRole(Role.ARTIST);

    }

}