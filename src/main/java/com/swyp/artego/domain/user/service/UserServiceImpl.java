package com.swyp.artego.domain.user.service;

import com.swyp.artego.domain.follow.repository.FollowRepository;
import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {
        userRepository.save(request.toEntity());
    }



    @Override
    @Transactional(readOnly = true)
    public UserInfoSimpleResponse getMyUserInfo(String oauthId) {
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new ServiceException("해당 유저를 찾을 수 없습니다."));
        return UserInfoSimpleResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistDetailInfoResponse getArtistDetailInfo(Long artistId, String viewerOauthId) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new ServiceException("해당 작가를 찾을 수 없습니다."));

        if (artist.isBanned() || artist.isDeleted() || !artist.getRole().isArtist()) {
            throw new ServiceException("활성화된 작가만 조회할 수 있습니다.");
        }

        User viewer = userRepository.findByOauthId(viewerOauthId)
                .orElseThrow(() -> new ServiceException("요청한 유저를 찾을 수 없습니다."));

        Boolean isFollowing = followRepository.findByUserAndUserArtist(viewer, artist).isPresent();

        return ArtistDetailInfoResponse.from(artist, isFollowing);
    }






}
