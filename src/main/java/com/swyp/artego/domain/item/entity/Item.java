package com.swyp.artego.domain.item.entity;

import com.swyp.artego.domain.item.enums.SizeType;
import com.swyp.artego.domain.item.enums.StatusType;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import com.swyp.artego.global.converter.BooleanToYNConverter;
import com.swyp.artego.global.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name="user_id")
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Convert(converter = StringListConverter.class)
    @Column(name= "img_url", nullable = false)
    private List<String> imgUrl;

    @Column(name = "price")
    private int price;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "secret", length = 1, nullable = false)
    private boolean isSecret; // ** 추가

    @Column(name = "size", nullable = false)
    @Enumerated(EnumType.STRING)
    private SizeType sizeType;

    @Column(name = "size_length")
    private int sizeLength;

    @Column(name = "size_width")
    private int sizeWidth;

    @Column(name = "size_heigth")
    private int sizeHeight;

    @Column(name= "material")
    private String material;

    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusType statusType = StatusType.OPEN;

    @Column(name = "category_type", nullable = false)
    //@Enumerated(EnumType.STRING)
    private String categoryType;

    @Builder
    public Item (
            User user, String title, String description, List<String> imgUrl, int price, boolean isSecret,
            SizeType sizeType, int sizeLength, int sizeWidth, int sizeHeight,
            String material, StatusType statusType, String categoryType
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.imgUrl = imgUrl;
        this.price = price;
        this.isSecret = isSecret;
        this.sizeType = sizeType;
        this.sizeLength = sizeLength;
        this.sizeWidth = sizeWidth;
        this.sizeHeight = sizeHeight;
        this.material = material;
        this.statusType = statusType;
        this.categoryType = categoryType;
    }


}
