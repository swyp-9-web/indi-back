package com.swyp.artego.domain.user.service;

import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.response.UserInfoResponse;
import com.swyp.artego.domain.user.dto.response.UserInfoSimpleResponse;

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
     * 유저 전체 조회
     * @return 유저 목록
     */
    List<UserInfoResponse> getAllUsers();



    // TODO: 사용자 프로필, 아티스트 프로필 (이때 유저 식별자값도 보내줘야 나중에 사용자가 아티스트를 팔로우 가능)



}
