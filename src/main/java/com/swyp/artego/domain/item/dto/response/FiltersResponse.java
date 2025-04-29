package com.swyp.artego.domain.item.dto.response;

import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltersResponse {

    private List<SizeType> size;
    private List<CategoryType> category;
}
