package com.swyp.artego.domain.item.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ItemInfoResponse {

    private Long itemId;
    private Artist artist;
    private String title;
    private String description;
    private List<String> imgUrls;
    private int price;
    private ItemSize size;
    private String material;
    private StatusType statusType;
    private CategoryType categoryType;


    @Getter
    @AllArgsConstructor
    public static class Artist {
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class ItemSize {
        private SizeType sizeType;
        private int width;
        private int height;
        private int depth;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Item 엔티티 -> DTO 변환 메서드
     */
    public static ItemInfoResponse fromEntity(Item item) {
        return ItemInfoResponse.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .artist(new Artist(item.getUser().getName()))
                .description(item.getDescription())
                .imgUrls(item.getImgUrls())
                .price(item.getPrice())
                .size(new ItemSize(item.getSizeType(), item.getSizeWidth(), item.getSizeHeight(), item.getSizeDepth()))
                .material(item.getMaterial())
                .statusType(item.getStatusType())
                .categoryType(item.getCategoryType())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
