package com.swyp.artego.global.file.controller;

import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import com.swyp.artego.global.file.dto.response.FileResponse;
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/upload")
    public String getUpload(){
        return "upload";
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFiles(
            @RequestPart(value = "files") List<MultipartFile> multipartFiles) {

        List<FileResponse> res = fileService.uploadFiles(multipartFiles, "file_domain");

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.FILE_UPLOAD_SUCCESS.getStatus())
                .resultMessage(SuccessCode.FILE_UPLOAD_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }
}
