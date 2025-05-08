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
    public static class Artist {
        private String name;
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
    }


    public static ItemFindByItemIdResponse fromEntity(Item item) {
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
                .size(
                        ItemSize.builder()
                                .sizeType(item.getSizeType())
                                .width(item.getSizeWidth())
                                .height(item.getSizeHeight())
                                .depth(item.getSizeDepth())
                                .build()
                )
                .material(item.getMaterial())
                .categoryType(item.getCategoryType())
                .artist(
                        Artist.builder()
                                .name(artist.getName())
                                .description(artist.getArtistAboutMe())
                                .build()
                )
                .reaction(
                        Reaction.builder()
                                .totalCount(likes + wants + revisits)
                                .likes(likes)
                                .wants(wants)
                                .revisits(revisits)
                                .build()
                )
                .build();
    }
}
