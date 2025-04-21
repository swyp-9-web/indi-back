package com.swyp.artego.domain.code.dto.request;

import com.swyp.artego.domain.code.entity.Code;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "코드 생성 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CodeCreateRequest {

    @Schema(description = "그룹 코드")
    @NotBlank(message = "grpCd is mandatory")
    @Size(min = 1, max = 16, message = "grpCd must be between 1 and 16")
    private String grpCd;

    @NotBlank
    @Size(min = 1, max = 16)
    private String cd;

    @NotBlank
    @Size(min = 1, max = 50)
    private String grpCdNm;

    @NotBlank
    @Size(min = 1, max = 50)
    private String cdNm;

    @Min(1)
    private int sortOrder;

    private boolean useYn;

    public Code toEntity() {
        return Code.builder()
                .grpCd(this.grpCd)
                .cd(this.cd)
                .grpCdNm(this.grpCdNm)
                .cdNm(this.cdNm)
                .build();
    }
}
