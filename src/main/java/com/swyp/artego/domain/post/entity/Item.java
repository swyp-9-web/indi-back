package com.swyp.artego.domain.post.entity;

import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "size")
    private String size; // TODO: 사이즈 저장 형식 기획 아직 안됨.

    @Column(name = "status", length = 4, nullable = false)
    // status = "OPEN" or "HIDE"
    private String status = "OPEN";

    @Column(name = "category_type", nullable = false)
    private String categoryType;

    @Builder
    public Item(User user, String title, String description, int price, int quantity, String size, String status, String categoryType) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.size = size;
        this.status = status;
        this.categoryType = categoryType;
    }
}
