package com.swyp.artego.domain.user.service;

import com.swyp.artego.domain.follow.repository.FollowRepository;

import com.swyp.artego.domain.user.dto.request.ArtistUpdateRequest;
import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.request.UserUpdateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));
        return UserInfoSimpleResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistDetailInfoResponse getArtistDetailInfo(Long artistId, AuthUser authUser) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessExceptionHandler("해당작가가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (artist.isBanned() || artist.isDeleted() || !artist.getRole().isArtist()) {
            throw new BusinessExceptionHandler("활성화된 작가만 조회할 수 있습니다.", ErrorCode.FORBIDDEN_ERROR);
        }


        Boolean isFollowing = false;

        //로그인 상태면
        if(authUser != null){
            User viewer = userRepository.findByOauthId(authUser.getOauthId())
                    .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));
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
                .orElseThrow(() -> new BusinessExceptionHandler("해당작가가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        // 아티스트 권한 체크
        if (!artist.getRole().isArtist()) {
            throw new BusinessExceptionHandler("아티스트만 프로필을 수정할 수 있습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        // 닉네임이 현재 닉네임과 다를 경우에만 중복 체크
        String newNickname = artistRequest.getNickname();
        if (newNickname != null && !newNickname.equals(artist.getNickname())) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new BusinessExceptionHandler("이미 사용 중인 닉네임입니다.", ErrorCode.DUPLICATE_RESOURCE);
            }
            artist.changeNickname(newNickname);
        }

        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            fileService.deleteFile(artist.getImgUrl());
            String newImgUrl = fileService.uploadFile(profileImage, folderName).getUploadFileUrl();
            artist.changeImgUrl(newImgUrl);
        }

        // 나머지 필드는 무조건 업데이트 (값이 null일 수 없다는 전제)
        artist.changeAboutMe(artistRequest.getAboutMe());
        artist.changeHomeLink(artistRequest.getHomeLink());
        artist.changeSnsLinks(artistRequest.getSnsLinks());

        return ArtistDetailInfoResponse.from(artist, false);
    }

    @Override
    @Transactional
    public UserInfoSimpleResponse updateUserProfile(AuthUser authUser, UserUpdateRequest request, MultipartFile profileImage) {
        User user = userRepository.findByOauthId(authUser.getOauthId())
                .orElseThrow(() -> new BusinessExceptionHandler("유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        // 닉네임 변경
        String newNickname = request.getNickname();
        if (newNickname != null && !newNickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new BusinessExceptionHandler("이미 사용 중인 닉네임입니다.", ErrorCode.DUPLICATE_RESOURCE);
            }
            user.changeNickname(newNickname);
        }

        // 프로필 이미지 변경
        if (profileImage != null && !profileImage.isEmpty()) {
            fileService.deleteFile(user.getImgUrl());
            String newImgUrl = fileService.uploadFile(profileImage, folderName).getUploadFileUrl();
            user.changeImgUrl(newImgUrl);
        }

        return UserInfoSimpleResponse.fromEntity(user);
    }


}
