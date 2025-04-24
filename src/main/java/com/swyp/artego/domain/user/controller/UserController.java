package com.swyp.artego.domain.user.controller;

import com.swyp.artego.domain.user.dto.request.UserCreateRequest;
import com.swyp.artego.domain.user.dto.response.UserInfoResponse;
import com.swyp.artego.domain.user.service.UserService;
import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 유저 생성 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUser(@RequestBody UserCreateRequest request) {
        userService.createUser(request);
        return ResponseEntity.status(SuccessCode.INSERT_SUCCESS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .result(null)
                        .resultCode(Integer.parseInt(SuccessCode.INSERT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.INSERT_SUCCESS.getMessage())
                        .build());
    }

    /**
     * 유저 전체 조회 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getAllUsers() {
        List<UserInfoResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.<List<UserInfoResponse>>builder()
                        .result(users)
                        .resultCode(Integer.parseInt(SuccessCode.SELECT_SUCCESS.getCode()))
                        .resultMessage(SuccessCode.SELECT_SUCCESS.getMessage())
                        .build()
        );
    }


}
