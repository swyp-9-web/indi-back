package com.swyp.artego.global.dummy.controller;

import com.swyp.artego.global.dummy.service.DummyCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
public class DummyCommentController {

    private final DummyCommentService dummyCommentService;

    @Operation(
            summary = "더미 댓글 생성",
            description = "개발용으로 더미 댓글을 여러 개 생성합니다." +
                    "쓰레드 참여자만 볼 수 있는 비밀 댓글, 삭제된 댓글 등이 포함되어 있습니다." +
                    "로그인을 하지 않아도 더미 댓글을 생성할 수 있습니다."
    )
    @PostMapping("/comments/dummy")
    public ResponseEntity<String> createDummyComments(
            @Parameter(description = "댓글을 생성할 작품의 id")
            @RequestParam Long itemId
    ) {
        dummyCommentService.createDummyComments(itemId);
        return ResponseEntity.ok("댓글 생성 완료!");
    }

}
