package com.swyp.artego.domain.post.entity;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
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

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "size")
    private String size; // TODO: 사이즈 저장 형식 기획 아직 안됨.

    @Column(name= "material")
    private String material;

    @Column(name = "status", length = 4, nullable = false)
    // status = "OPEN" or "HIDE" or "TEMP"
    private String status = "OPEN";

    @Column(name = "category_type", nullable = false)
    private String categoryType;

    @Builder
    public Item(User user, String title, String description, List<String> imgUrl, int price, String size, String material, String status, String categoryType) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.imgUrl = imgUrl;
        this.price = price;
        this.size = size;
        this.material = material;
        this.status = status;
        this.categoryType = categoryType;
    }
}
