package com.swyp.artego.global.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class FileViewController {

    @GetMapping("/upload")
    public String getUpload() {
        return "upload";  // templates/upload.html 렌더링
    }
}
