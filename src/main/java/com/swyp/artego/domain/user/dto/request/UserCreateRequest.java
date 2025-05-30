package com.swyp.artego.domain.user.dto.request;

import com.swyp.artego.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Schema(description = "유저 생성 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCreateRequest {

    @Schema(description = "OAuth ID (프로바이더+유저ID)", example = "kakao 123456789")
    //@NotBlank(message = "oauthId는 필수입니다.")
    private String oauthId;

    @Schema(description = "이름", example = "홍길동")
    //@NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "이메일", example = "test@example.com")
    //@NotBlank(message = "이메일은 필수입니다.")
    //@Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    //@NotBlank(message = "전화번호는 필수입니다.")
    //@Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String telNumber;

    /**
     * User Entity 변환 메서드
     */
    public User toEntity() {
        return User.builder()
                .oauthId(this.oauthId)
                .name(this.name)
                .email(this.email)
                .telNumber(this.telNumber)
                .build();
    }
}
