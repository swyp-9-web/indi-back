package com.swyp.artego.domain.comment.dto.response;

import com.swyp.artego.domain.item.dto.response.MetaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyCommentActivityResultResponse {
    private List<MyCommentActivityResponse> comments;
    private MetaResponse meta;
}