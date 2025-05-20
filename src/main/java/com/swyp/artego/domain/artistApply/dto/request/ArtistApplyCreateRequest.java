package com.swyp.artego.domain.artistApply.dto.request;

import com.swyp.artego.domain.artistApply.entity.ArtistApply;
import com.swyp.artego.domain.artistApply.enums.Status;
import com.swyp.artego.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ArtistApplyCreateRequest {

    @NotBlank
    @Size(max = 3000)
    private String artistAboutMe;

    @Email(message = "올바르지 않은 이메일 형식입니다.")
    @NotBlank
    @Size(max = 50)
    @Schema(example = "artego-email-example@gmail.com")
    private String email;

    @NotBlank
    @Size(max = 50)
    private String snsLink;

    /**
     * 엔티티로 변환하는 메서드
     */
    public ArtistApply toEntity(User user, long rejectedHistoryCounts) {
        return ArtistApply.builder()
                .user(user)
                .artistAboutMe(this.artistAboutMe)
                .email(this.email)
                .snsLink(this.snsLink)
                .applyStatus(Status.PENDING)
                .rejectedCount((int) rejectedHistoryCounts)
                .build();
    }
}