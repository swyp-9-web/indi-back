package com.swyp.artego.domain.artistApply.service;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.request.ConvertToArtistRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyFindAllResponse;
import com.swyp.artego.domain.artistApply.enums.Status;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistApplyServiceImpl implements ArtistApplyService {

    private final ArtistApplyRepository artistApplyRepository;

    private final UserRepository userRepository;

    @Transactional
    public ArtistApplyCreateResponse createArtistApply(AuthUser authUser, ArtistApplyCreateRequest request) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (user.getRole() != Role.USER) {
            throw new BusinessExceptionHandler("User 권한만 작가로 신청할 수 있습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        // TODO: 해당 기획 사용하는지에 대한 명재님 답변 이후 수정
//        boolean isPending = artistApplyRepository.existsByUserIdAndStatus(user.getId(), Status.PENDING);
//        if (isPending) {
//            throw new BusinessExceptionHandler("'승인 대기중' 상태에서는 중복 지원이 불가능합니다.", ErrorCode.BAD_REQUEST_ERROR);
//        }

        long rejectedHistoryCounts = artistApplyRepository.countAllByUserIdAndStatus(user.getId(), Status.REJECTED);

        return ArtistApplyCreateResponse.fromEntity(
                artistApplyRepository.save(request.toEntity(user, rejectedHistoryCounts))
        );
    }

    @Transactional(readOnly = true)
    public List<ArtistApplyFindAllResponse> getArtistApplies() {

        return artistApplyRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ArtistApplyFindAllResponse::fromEntity)
                .collect(Collectors.toList());
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