package com.swyp.artego.domain.user.dto.response;

import com.swyp.artego.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "유저 정보 응답 DTO")
@Getter
@AllArgsConstructor
@Builder
public class UserInfoResponse {

    @Schema(description = "유저 이름")
    private String name;

    @Schema(description = "유저 이메일")
    private String email;

    @Schema(description = "유저 전화번호")
    private String telNumber;


    /**
     * Entity -> DTO 변환 메서드
     */
    public static UserInfoResponse fromEntity(User user) {
        return UserInfoResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .telNumber(user.getTelNumber())
                .build();
    }
}
