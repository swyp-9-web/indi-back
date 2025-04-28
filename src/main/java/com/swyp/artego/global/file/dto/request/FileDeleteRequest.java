package com.swyp.artego.global.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "파일 삭제 요청 DTO")
public class FileDeleteRequest {
    private String imgUrl;
}
