package com.swyp.artego.domain.item.dto.response;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ItemFindByItemIdResponse {

    private Long itemId;
    private String title;
    private String description;
    private List<String> imgUrls;
    private int price;
    private ItemSize size;
    private String material;
    private CategoryType categoryType;

    private Long totalScrapCount;

    private ViewerInfo viewer;
    private Artist artist;
    private Reaction reaction;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ItemSize {
        private SizeType sizeType;
        private int width;
        private int height;
        private int depth;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ViewerInfo {
        private Boolean isScrapped;
        private Boolean isFollowing;
        private Boolean isOwner;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Artist {
        private Long id;
        private String nickname;
        private String profileImgUrl;
        private String description;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Reaction {
        private int totalCount;
        private int likes;
        private int wants;
        private int revisits;

        private Boolean isLiked;
        private Boolean isWanted;
        private Boolean isRevisited;

        private Long likedEmojiId;
        private Long wantedEmojiId;
        private Long revisitedEmojiId;
    }

    public static ItemFindByItemIdResponse fromEntity(
            Item item, Long totalScrapCount, boolean isScrapped, boolean isFollowing,
            boolean isOwner, boolean isLiked, boolean isWanted, boolean isRevisited,
            Long likedEmojiId, Long wantedEmojiId, Long revisitedEmojiId
    ) {
        User artist = item.getUser();

        int likes = item.getLikeCount();
        int wants = item.getWantCount();
        int revisits = item.getRevisitCount();

        return ItemFindByItemIdResponse.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .imgUrls(item.getImgUrls())
                .price(item.getPrice())
                .size(ItemSize.builder()
                        .sizeType(item.getSizeType())
                        .width(item.getSizeWidth())
                        .height(item.getSizeHeight())
                        .depth(item.getSizeDepth())
                        .build())
                .material(item.getMaterial())
                .categoryType(item.getCategoryType())
                .totalScrapCount(totalScrapCount)
                .viewer(
                        ViewerInfo.builder()
                        .isScrapped(isScrapped)
                        .isFollowing(isFollowing)
                        .isOwner(isOwner)
                        .build()
                )
                .artist(
                        Artist.builder()
                        .id(artist.getId())
                        .nickname(artist.getNickname())
                        .profileImgUrl(artist.getImgUrl())
                        .description(artist.getArtistAboutMe())
                        .build()
                )

                .reaction(
                        Reaction.builder()
                        .totalCount(likes + wants + revisits)
                        .likes(likes)
                        .wants(wants)
                        .revisits(revisits)
                        .isLiked(isLiked)
                        .isWanted(isWanted)
                        .isRevisited(isRevisited)
                        .likedEmojiId(likedEmojiId)
                        .wantedEmojiId(wantedEmojiId)
                        .revisitedEmojiId(revisitedEmojiId)
                        .build()
                )
                .build();
    }
}
