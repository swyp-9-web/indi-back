package com.swyp.artego.domain.item.dto.request;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "이미지를 제외한 작품 정보")
public class ItemCreateRequest {

    @NotBlank
    @Size(max = 40)
    private String title;

    private CategoryType categoryType;

    @Valid
    private ItemSize size;

    @Size(max = 40)
    private String material;

    @NotBlank
    @Size(max = 400)
    private String description;

    @PositiveOrZero
    private int price;

    private StatusType statusType;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "작품의 사이즈 정보 (가로, 세로, 폭) - 04/30 피그마 기준 용어")
    public static class ItemSize {
        @Schema(description = "가로 (cm)", example = "10")
        @PositiveOrZero
        private int width;
        @Schema(description = "세로 (cm)", example = "50")
        @PositiveOrZero
        private int height;
        @Schema(description = "폭 (cm)", example = "30")
        @PositiveOrZero
        private int depth;
    }


    /**
     * Item 엔티티로 변환하는 메서드
     */
    public Item toEntity(User user, List<String> imgUrls, SizeType sizeType) {
        return Item.builder()
                .user(user)
                .title(this.title)
                .description(this.description)
                .imgUrls(imgUrls)
                .price(this.price)
                .sizeType(sizeType)
                .sizeWidth(this.size.getWidth())
                .sizeHeight(this.size.getHeight())
                .sizeDepth(this.size.getDepth())
                .material(this.material)
                .statusType(statusType != null ? statusType : StatusType.OPEN)
                .categoryType(this.categoryType)
                .build();
    }
}
