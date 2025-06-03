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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    private final FileService fileService;
    private final UserPersistenceService userPersistenceService;

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
        if (authUser != null) {
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
    public ArtistDetailInfoResponse updateArtistProfile(AuthUser authUser, ArtistUpdateRequest artistRequest, MultipartFile profileImage) {
        User artist = userPersistenceService.loadAndValidateArtist(authUser.getOauthId());
        boolean isNicknameChanged = userPersistenceService.isNicknameChangedAndAvailable(artist.getNickname(), artistRequest.getNickname());

        // 프로필 이미지 처리
        String newImgUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            newImgUrl = fileService.uploadFile(profileImage, folderName).getUploadFileUrl();
        }

        // 이미지를 제외한 필드는 무조건 업데이트 (값이 null일 수 없다는 전제)
        return userPersistenceService.updateArtistDetailWithTransaction(artist, artistRequest, newImgUrl, isNicknameChanged);
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
