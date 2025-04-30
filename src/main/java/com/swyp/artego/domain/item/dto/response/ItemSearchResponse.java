package com.swyp.artego.domain.item.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
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

    @Getter
    public static class Artist {
        private Long id;
        private String nickname;

        public Artist(Long id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }
    }

    @Getter
    public static class Scrap {
        private Boolean isScrapped;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime scrapedAt;

        public Scrap(Boolean isScrapped, LocalDateTime scrapedAt) {
            this.isScrapped = isScrapped;
            this.scrapedAt = scrapedAt;
        }
    }

    @Getter
    public static class Reaction {
        private int likes;
        private int wants;
        private int revisits;

        public Reaction(int likes, int wants, int revisits) {
            this.likes = likes;
            this.wants = wants;
            this.revisits = revisits;
        }
    }
}
