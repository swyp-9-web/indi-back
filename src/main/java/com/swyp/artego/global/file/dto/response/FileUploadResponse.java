package com.swyp.artego.global.file.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileUploadResponse {
    List<String> imgUrls;
}
