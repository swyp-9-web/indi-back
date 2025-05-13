package com.swyp.artego.domain.user.dto.response;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoSimpleResponse {

    private Long id;
    private String nickname;
    private String profileImgUrl;
    private String email;
    private Role role;
    private String telNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    public static UserInfoSimpleResponse fromEntity(User user) {
        return UserInfoSimpleResponse.builder()
                .id(user.getId())
                .profileImgUrl(user.getImgUrl())
                .email(user.getEmail())
                .role(user.getRole())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .telNumber(user.getTelNumber())
                .build();
    }


}
