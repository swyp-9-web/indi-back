package com.swyp.artego.domain.item.entity;

import com.swyp.artego.domain.item.enums.CategoryType;
import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import com.swyp.artego.global.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item")
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Column(name = "description", nullable = false, length = 810) // 공백 제외 400자 제한이므로 *2 +@
    private String description;

    @Setter
    @Convert(converter = StringListConverter.class)
    @Column(name = "img_urls", nullable = false, length = 1000) // 한 url당 약 110바이트 내외 * 최대 8장
    private List<String> imgUrls;

    @Setter
    @Column(name = "price")
    private int price;

    @Setter
    @Column(name = "size", nullable = false)
    @Enumerated(EnumType.STRING)
    private SizeType sizeType;

    @Setter
    @Column(name = "size_width") // 가로
    private int sizeWidth;

    @Setter
    @Column(name = "size_heigth") // 세로
    private int sizeHeight;

    @Setter
    @Column(name = "size_depth") // 폭
    private int sizeDepth;

    @Setter
    @Column(name = "material")
    private String material;

    @Setter
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusType statusType = StatusType.OPEN;

    @Setter
    @Column(name = "category_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    @Column(name = "scrap_count", nullable = false)
    private int scrapCount = 0;


    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;


    @Column(name = "want_count", nullable = false)
    private int wantCount = 0;


    @Column(name = "revisit_count", nullable = false)
    private int revisitCount = 0;


    @Column(name = "total_reaction_score", nullable = false)
    private int totalReactionCount = 0;


    @Builder
    public Item(
            User user, String title, String description, List<String> imgUrls, int price,
            SizeType sizeType, int sizeWidth, int sizeHeight, int sizeDepth,
            String material, StatusType statusType, CategoryType categoryType
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.imgUrls = imgUrls;
        this.price = price;
        this.sizeType = sizeType;
        this.sizeDepth = sizeDepth;
        this.sizeWidth = sizeWidth;
        this.sizeHeight = sizeHeight;
        this.material = material;
        this.statusType = (statusType != null) ? statusType : StatusType.OPEN;
        this.categoryType = categoryType;
    }


    public void updateCounts(int scrap, int like, int want, int revisit, int totalReaction) {
        this.scrapCount = scrap;
        this.likeCount = like;
        this.wantCount = want;
        this.revisitCount = revisit;
        this.totalReactionCount = totalReaction;
    }


}
