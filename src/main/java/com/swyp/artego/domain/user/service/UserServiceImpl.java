package com.swyp.artego.domain.user.service;

import com.swyp.artego.domain.follow.repository.FollowRepository;

import com.swyp.artego.domain.user.dto.request.ArtistUpdateRequest;
import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    private final FileService fileService;

    @Value("${ncp.storage.bucket.folder.user-profile}")
    private String folderName;

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
    public ArtistDetailInfoResponse getArtistDetailInfo(Long artistId, AuthUser authUser) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new ServiceException("해당 작가를 찾을 수 없습니다."));

        if (artist.isBanned() || artist.isDeleted() || !artist.getRole().isArtist()) {
            throw new ServiceException("활성화된 작가만 조회할 수 있습니다.");
        }


        Boolean isFollowing = false;

        //로그인 상태면
        if(authUser != null){
            User viewer = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new ServiceException("요청한 유저를 찾을 수 없습니다."));
           isFollowing = followRepository.findByUserAndUserArtist(viewer, artist).isPresent();
        }

        return ArtistDetailInfoResponse.from(artist, isFollowing);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    @Override
    @Transactional
    public ArtistDetailInfoResponse updateArtistProfile(AuthUser authUser, ArtistUpdateRequest artistRequest, MultipartFile profileImage) {
        User artist = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new ServiceException("해당 작가를 찾을 수 없습니다."));

        //  아티스트 권한 체크
        if (!artist.getRole().isArtist()) {
            throw new ServiceException("아티스트만 프로필을 수정할 수 있습니다.");
        }

        // 닉네임 중복 체크 (변경 시에만)
        if (artistRequest.getNickname() != null
                && userRepository.existsByNickname(artistRequest.getNickname())) {
            throw new ServiceException("이미 사용 중인 닉네임입니다.");
        }

        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {

            // 기존 이미지 삭제
            fileService.deleteFile(artist.getImgUrl());

            // 새 이미지 업로드 및 반영
            String newImgUrl = fileService.uploadFile(profileImage, folderName).getUploadFileUrl();
            artist.changeImgUrl(newImgUrl);
        }

        // 프로필 정보 수정
        artist.updateArtistProfile(
                artistRequest.getNickname(),
                artistRequest.getAboutMe(),
                artistRequest.getHomeLink(),
                artistRequest.getSnsLinks()
        );

        return ArtistDetailInfoResponse.from(artist, false);
    }








}
