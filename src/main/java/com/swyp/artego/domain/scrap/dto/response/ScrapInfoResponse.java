package com.swyp.artego.domain.scrap.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.artego.domain.scrap.entity.Scrap;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScrapInfoResponse {

    private Long scrapId;
    private Long itemId;
    private String itemName;
    private String userName;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static ScrapInfoResponse fromEntity(Scrap scrap) {
        return ScrapInfoResponse.builder()
                .scrapId(scrap.getId())
                .itemId(scrap.getItem().getId())
                .itemName(scrap.getItem().getTitle())
                .userName(scrap.getUser().getName())
                .createdAt(scrap.getCreatedAt())
                .build();
    }
}
