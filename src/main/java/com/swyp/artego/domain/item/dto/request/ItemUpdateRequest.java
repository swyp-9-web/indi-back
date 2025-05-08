package com.swyp.artego.domain.item.dto.request;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "이미지를 제외한 작품 정보")
public class ItemUpdateRequest {

    @NotBlank
    @Size(max = 40)
    private String title;

    private CategoryType categoryType;

    @Valid
    @NotNull(message = "사이즈 정보는 필수입니다. 측정이 불가하다면 숫자 0을 작성해주세요.")
    private ItemCreateRequest.ItemSize size;

    @Size(max = 40)
    private String material;

    @NotBlank
    @Size(max = 400)
    private String description;

    @PositiveOrZero
    private int price;

    private StatusType statusType;

    private List<String> imageOrder;

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

    @Schema(hidden = true)
    @AssertTrue(message = "사이즈는 (0x0x0), (양수x양수x0), 또는 (양수x양수x양수) 형태만 가능합니다.")
    public boolean isValidSizeCombination() {

        int w = size.getWidth();
        int h = size.getHeight();
        int d = size.getDepth();

        boolean isAllZero = (w == 0 && h == 0 && d == 0);
        boolean isDepthZero = (w > 0 && h > 0 && d == 0);
        boolean isAllPositive = (w > 0 && h > 0 && d > 0);

        return isAllZero || isDepthZero || isAllPositive;
    }

    /**
     * Item 엔티티를 업데이트는 메서드
     */
    public void applyToEntity(Item item, List<String> imgUrls, SizeType sizeType) {
        item.setTitle(this.title);
        item.setDescription(this.description);
        item.setImgUrls(imgUrls);
        item.setPrice(this.price);
        item.setSizeType(sizeType);
        item.setSizeWidth(this.size.getWidth());
        item.setSizeHeight(this.size.getHeight());
        item.setSizeDepth(this.size.getDepth());
        item.setMaterial(this.material);
        item.setStatusType(this.statusType);
        item.setCategoryType(this.categoryType);
    }
}
