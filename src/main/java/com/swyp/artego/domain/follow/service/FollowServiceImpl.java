package com.swyp.artego.domain.follow.service;

import com.swyp.artego.domain.follow.dto.response.FollowInfoResponse;
import com.swyp.artego.domain.follow.entity.Follow;
import com.swyp.artego.domain.follow.repository.FollowRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.common.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createFollow(AuthUser authUser, Long artistId) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessExceptionHandler("아티스트가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Follow follow = Follow.builder()
                .user(user)
                .userArtist(artist)
                .build();

        followRepository.save(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowInfoResponse> getAllFollows() {
        return followRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(FollowInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }


}
