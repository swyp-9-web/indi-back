package com.swyp.artego.domain.user.dto.response;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoSimpleResponse {

    private Long id;
    private String profileImgUrl;
    private String email;
    private Role role;

    public static UserInfoSimpleResponse fromEntity(User user) {
        return UserInfoSimpleResponse.builder()
                .id(user.getId())
                .profileImgUrl(user.getImgUrl())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

}
