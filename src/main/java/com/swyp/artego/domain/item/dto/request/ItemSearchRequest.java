package com.swyp.artego.domain.item.dto.request;

import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.SortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "아이템 검색 요청 DTO")
public class ItemSearchRequest {

    @Schema(description = "검색 키워드 (작가명 또는 작품명)", example = "바다")
    private String keyword;

    @Schema(description = "카테고리 필터 (여러 개 선택 가능)", example = "[\"TEXTILE_ART\", \"VISUAL_ART\"]")
    private List<CategoryType> categoryTypes;

    @Schema(description = "사이즈 필터 (여러 개 선택 가능)", example = "[\"S\", \"M\", \"L\"]")
    private List<SizeType> sizeTypes;

    @Schema(description = "정렬 방식", example = "CREATED_RECENT")
    private SortType sortType;

    @Schema(description = "조회할 작가 ID (선택값, 없으면 전체 조회)", example = "123")
    private Long artistId;

    @Schema(description = "페이지 번호 (0부터 시작)", defaultValue = "0", example = "0")
    private Integer page;

    @Schema(description = "페이지당 아이템 수", defaultValue = "10", example = "10")
    private Integer limit;
}
