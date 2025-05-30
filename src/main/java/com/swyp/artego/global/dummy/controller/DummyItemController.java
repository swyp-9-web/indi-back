package com.swyp.artego.global.dummy.controller;

import com.swyp.artego.global.dummy.service.DummyItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
public class DummyItemController {

    private final DummyItemService dummyItemService;

    @Operation(
            summary = "더미 아이템 생성",
            description = "개발용으로 더미 아이템을 여러 개 생성합니다. 기본은 100개 생성되며, 원하는 개수를 count 파라미터로 조절할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "더미 아이템 생성 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/items/dummy")
    public ResponseEntity<String> createDummyItems(
            @Parameter(description = "생성할 더미 아이템 개수 (기본값: 100)")
            @RequestParam(defaultValue = "100") int count
    ) {
        dummyItemService.createDummyData(count);
        return ResponseEntity.ok(count + "개의 더미 아이템 생성 완료!");
    }
}
