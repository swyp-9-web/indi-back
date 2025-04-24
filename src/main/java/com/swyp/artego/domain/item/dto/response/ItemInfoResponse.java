package com.swyp.artego.domain.item.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ItemInfoResponse {

    private Long itemId;
    private String title;
    private String artist_name;
    private String description;
    private List<String> imgUrl;
    private int price;
    private boolean isSecret;
    private SizeType sizeType;
    private int sizeLength;
    private int sizeWidth;
    private int sizeHeight;
    private String material;
    private StatusType statusType;
    private String categoryType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Item 엔티티 -> DTO 변환 메서드
     */
    public static ItemInfoResponse fromEntity(Item item) {
        return ItemInfoResponse.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .artist_name(item.getUser().getName())
                .description(item.getDescription())
                .imgUrl(item.getImgUrl())
                .price(item.getPrice())
                .isSecret(item.isSecret())
                .sizeType(item.getSizeType())
                .sizeLength(item.getSizeLength())
                .sizeWidth(item.getSizeWidth())
                .sizeHeight(item.getSizeHeight())
                .material(item.getMaterial())
                .statusType(item.getStatusType())
                .categoryType(item.getCategoryType())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
