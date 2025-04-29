package com.swyp.artego.domain.item.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "아이템 생성 요청 DTO")
public class ItemCreateRequest {

    private String title;
    private String description;
    private List<String> imgUrl;
    private int price;

    @JsonProperty(value = "isSecret")
    private boolean isSecret;
    private int sizeLength;
    private int sizeWidth;
    private int sizeHeight;
    private String material;
    private StatusType statusType;
    private CategoryType categoryType;




    /**
     * Item 엔티티 변환 메서드
     */
    public Item toEntity(User user, SizeType sizeType) {
        return Item.builder()
                .user(user)
                .title(this.title)
                .description(this.description)
                .imgUrl(this.imgUrl)
                .price(this.price)
                .isSecret(this.isSecret)
                .sizeType(sizeType)
                .sizeLength(this.sizeLength)
                .sizeWidth(this.sizeWidth)
                .sizeHeight(this.sizeHeight)
                .material(this.material)
                .statusType(statusType != null ? statusType : StatusType.OPEN)
                .categoryType(this.categoryType)
                .build();
    }
}
