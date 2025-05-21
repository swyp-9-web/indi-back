package com.swyp.artego.domain.artistApply.service;

import com.swyp.artego.domain.artistApply.dto.request.ArtistApplyCreateRequest;
import com.swyp.artego.domain.artistApply.dto.request.ConvertToArtistRequest;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyCreateResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyFindAllResponse;
import com.swyp.artego.domain.artistApply.dto.response.ArtistApplyListResponse;
import com.swyp.artego.domain.artistApply.entity.ArtistApply;
import com.swyp.artego.domain.artistApply.enums.Status;
import com.swyp.artego.domain.artistApply.repository.ArtistApplyRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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


        boolean isPending = artistApplyRepository.existsByUserIdAndStatus(user.getId(), Status.PENDING);
        if (isPending) {
            throw new BusinessExceptionHandler("'승인 대기중' 상태에서는 중복 지원이 불가능합니다.", ErrorCode.BAD_REQUEST_ERROR);
        }

        long rejectedHistoryCounts = artistApplyRepository.countAllByUserIdAndStatus(user.getId(), Status.REJECTED);

        return ArtistApplyCreateResponse.fromEntity(
                artistApplyRepository.save(request.toEntity(user, rejectedHistoryCounts))
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ArtistApplyListResponse getArtistApplies(int page, int size) {
        int internalPage = Math.max(page - 1, 0);
        PageRequest pageable = PageRequest.of(internalPage, size);

        Page<ArtistApply> result = artistApplyRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<ArtistApplyFindAllResponse> content = result.getContent()
                .stream()
                .map(ArtistApplyFindAllResponse::fromEntity)
                .toList();

        ArtistApplyListResponse.Meta meta = ArtistApplyListResponse.Meta.builder()
                .currentPage(page)
                .pageSize(size)
                .totalApplies(result.getTotalElements())
                .hasNextPage(result.hasNext())
                .build();

        return ArtistApplyListResponse.builder()
                .applies(content)
                .meta(meta)
                .build();
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

        // 작가지원 이력 조회
        ArtistApply artistApply = artistApplyRepository.findById(request.getArtistApplyId())
                .orElseThrow(() -> new BusinessExceptionHandler("작가지원 이력을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_ERROR));

        //작가 지원 승인
        artistApply.approve();

        // 작가 권한 부여
        target.setRole(Role.ARTIST);
    }

    @Transactional
    @Override
    public void rejectArtistApply(AuthUser adminUser, ConvertToArtistRequest request) {
        User admin = userRepository.findByOauthId(adminUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("관리자 계정이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!admin.getRole().equals(Role.ADMIN)) {
            throw new BusinessExceptionHandler("접근 권한이 없습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        ArtistApply artistApply = artistApplyRepository.findById(request.getArtistApplyId())
                .orElseThrow(() -> new BusinessExceptionHandler("작가 신청 이력이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        artistApply.reject();
    }



}