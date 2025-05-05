package com.swyp.artego.domain.follow.service;

import com.swyp.artego.domain.follow.dto.response.FollowInfoResponse;
import com.swyp.artego.domain.follow.dto.response.FollowPreviewListResponse;
import com.swyp.artego.domain.follow.dto.response.FollowPreviewResponse;
import com.swyp.artego.domain.follow.dto.response.FollowedArtistsResponse;
import com.swyp.artego.domain.follow.entity.Follow;
import com.swyp.artego.domain.follow.repository.FollowRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.common.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        boolean alreadyFollowed = followRepository.findByUserAndUserArtist(user, artist).isPresent();
        if (alreadyFollowed) {
            throw new BusinessExceptionHandler("이미 팔로우한 아티스트입니다.", ErrorCode.DUPLICATE_RESOURCE);
        }

        Follow follow = Follow.builder()
                .user(user)
                .userArtist(artist)
                .build();

        followRepository.save(follow);
    }




    @Override
    @Transactional(readOnly = true)
    public FollowPreviewListResponse getFollowPreview(AuthUser authUser) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        List<Follow> recentFollows = followRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        int totalFollowings = followRepository.countByUser(user);

        List<FollowPreviewResponse> artistPreviewList = recentFollows.stream()
                .map(follow -> FollowPreviewResponse.fromEntity(follow.getUserArtist()))
                .toList();

        return FollowPreviewListResponse.builder()
                .totalFollowings(totalFollowings)
                .followingArtists(artistPreviewList)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public FollowedArtistsResponse getFollowedArtists(AuthUser authUser, int page, int size) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));


        return followRepository.findFollowedArtistsWithItems(user.getId(), page, size);

    }







    @Override
    @Transactional
    public void deleteFollow(AuthUser authUser, Long artistId) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessExceptionHandler("아티스트가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        Follow follow = followRepository.findByUserAndUserArtist(user, artist)
                .orElseThrow(() -> new BusinessExceptionHandler("팔로우 관계가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        followRepository.delete(follow);
    }



}
