package com.swyp.artego.global.file.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponseExample {

    private String originalFileName;
    private String uploadFileName;
    private String uploadFilePath;
    private String uploadKey;
    private String uploadFileUrl;
}
