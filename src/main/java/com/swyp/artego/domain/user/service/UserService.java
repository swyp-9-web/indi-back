package com.swyp.artego.domain.user.service;


import com.swyp.artego.domain.user.dto.request.ArtistUpdateRequest;
import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;
import com.swyp.artego.global.auth.oauth.model.AuthUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {


    /**
     * 유저 생성
     * @param request 유저 생성 요청 DTO
     * @return 없음
     */
    void createUser(UserCreateRequest request);

    /**
     * 유저 간단한 정보 조회
     * @param oauthId  유저 유니크 키
     * @return UserInfoSimpleResponse 유저 간단한 정보 DTO
     */

    UserInfoSimpleResponse getMyUserInfo(String oauthId);


    /**
     * 아티스트 자세한 정보 조회
     * @param artistId  아티스트 키
     * @return ArtistDetailInfoResponse 아티스트 자세한 정보 DTO
     */
    ArtistDetailInfoResponse getArtistDetailInfo(Long artistId, AuthUser authUser);

    /**
     * 아티스트 닉네임 중복 체크
     * @param nickname  아티스트 닉네임
     * @return boolean 중복여부
     */
    boolean isNicknameDuplicated(String nickname);


    /**
     * 아티스트 프로필 수정
     * @param authUser  로그인 유저
     * @param request  아티스트 변경 정보
     * @param profileImage  프로필 이미지
     * @return ArtistDetailInfoResponse 아티스트 자세한 정보 DTO
     */
    ArtistDetailInfoResponse updateArtistProfile(AuthUser authUser, ArtistUpdateRequest request, MultipartFile profileImage);



}
