package com.swyp.artego.global.file.controller;

import com.swyp.artego.global.common.code.SuccessCode;
import com.swyp.artego.global.common.response.ApiResponse;
import com.swyp.artego.global.file.dto.request.FileDeleteRequest;
import com.swyp.artego.global.file.dto.response.FileUploadResponse;
import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/test/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFiles(
            @RequestPart(value = "files") List<MultipartFile> multipartFiles) {

        List<String> imgUrls = fileService.uploadFiles(multipartFiles, "file_domain");

        FileUploadResponse res = FileUploadResponse.builder()
                .imgUrls(imgUrls)
                .build();

        ApiResponse ar = ApiResponse.builder()
                .result(res)
                .resultCode(SuccessCode.FILE_UPLOAD_SUCCESS.getStatus())
                .resultMessage(SuccessCode.FILE_UPLOAD_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }



    @PostMapping("/delete")
    public ResponseEntity<Object> deleteFile(
            @RequestBody FileDeleteRequest request) {

        fileService.deleteFile(request.getImgUrl());

        ApiResponse ar = ApiResponse.builder()
                .result(null)
                .resultCode(SuccessCode.FILE_DELETE_SUCCESS.getStatus())
                .resultMessage(SuccessCode.FILE_DELETE_SUCCESS.getMessage())
                .build();

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }
}
