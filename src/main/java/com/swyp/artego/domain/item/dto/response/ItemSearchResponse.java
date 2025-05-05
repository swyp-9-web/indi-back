package com.swyp.artego.domain.item.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchResponse {

    private Long id;
    private String thumbnailImgUrl;
    private String name;
    private int price;
    private CategoryType category;
    private SizeType size;

    private Artist artist;
    private Scrap scrap;
    private int totalScraped;
    private Reaction totalReaction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // QueryDSL 프로젝션용 생성자
    @QueryProjection
    public ItemSearchResponse(
            Long itemId,
            List<String> imgUrl,
            String title,
            int price,
            CategoryType categoryType,
            SizeType sizeType,
            Long artistId,
            String artistNickname,
            Boolean isScrapped,
            LocalDateTime scrapedAt,
            int totalScraped,
            int likeCount,
            int wantCount,
            int revisitCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = itemId;
        this.thumbnailImgUrl = (imgUrl != null && !imgUrl.isEmpty()) ? imgUrl.get(0) : null;
        this.name = title;
        this.price = price;
        this.category = categoryType;
        this.size = sizeType;
        this.artist = new Artist(artistId, artistNickname);
        this.scrap = new Scrap(isScrapped, scrapedAt);
        this.totalScraped = totalScraped;
        this.totalReaction = new Reaction(likeCount, wantCount, revisitCount);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //  기본 fromEntity (스크랩 정보 없음)
    public static ItemSearchResponse fromEntity(Item item) {
        return fromEntity(item, false, null);
    }

    //  스크랩 정보 포함하는 fromEntity
    public static ItemSearchResponse fromEntity(Item item, boolean isScrapped, LocalDateTime scrapedAt) {
        return ItemSearchResponse.builder()
                .id(item.getId())
                .thumbnailImgUrl(item.getImgUrls() != null && !item.getImgUrls().isEmpty() ? item.getImgUrls().get(0) : null)
                .name(item.getTitle())
                .price(item.getPrice())
                .category(item.getCategoryType())
                .size(item.getSizeType())
                .artist(new Artist(
                        item.getUser().getId(),
                        item.getUser().getName()
                ))
                .scrap(new Scrap(isScrapped, scrapedAt))
                .totalScraped(item.getScrapCount())
                .totalReaction(new Reaction(
                        item.getLikeCount(),
                        item.getWantCount(),
                        item.getRevisitCount()
                ))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    //  Artist 정보 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        private Long id;
        private String nickname;
    }

    //  Scrap 정보 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Scrap {
        private Boolean isScrapped;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime scrapedAt;
    }

    //  Reaction 정보 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reaction {
        private int likes;
        private int wants;
        private int revisits;
    }
}
