package com.swyp.artego.domain.user.entity;

import com.swyp.artego.domain.user.enums.Role;
import com.swyp.artego.global.common.entity.BaseTimeEntity;
import com.swyp.artego.global.converter.BooleanToYNConverter;
import com.swyp.artego.global.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "\"user\"") // SQL에서 예약어(reserved word) 이기 때문이야.=> user 쓰지말자!
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;


    // oauthId = 프로바이더 (카카오,네이버) + 그 소셜로그인에서의 유일한아이디 <- 만든계기 : 소셜로그인 여러가지 추가하게되면 나중에 이메일 겹칠수도있음

    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "tel_number", nullable = false)
    private String telNumber;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; // ex: "user", "artist"

    @Column(name = "img_url", nullable = false)
    private String imgUrl = "https://kr.object.ncloudstorage.com/artego-bucket/file_domain/b3db25fe-5e0e-485e-b342-91ee1239950d.jpg"; // 기본 프로필 이미지


    @Column(name = "artist_home_sns_info")
    private String artistHomeSnsInfo; // 개인 홈페이지용


    @Convert(converter = StringListConverter.class)
    @Column(name = "artist_sns_info")
    // '종류_링크' 형식으로 저장. 예시) insta_https://blahblah, youtube_https://blahblah
    private List<String> artistSnsInfo; // Nullable

    @Column(name = "artist_about_me")
    private String artistAboutMe; // Nullable // 자기소개

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "banned", length = 1, nullable = false)
    private boolean banned = false;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "deleted", length = 1, nullable = false)
    private boolean deleted = false;

    @Column(name = "item_count")
    private int itemCount = 0;

    @Column(name = "scrap_count")
    private int scrapCount = 0;

    @Column(name = "reaction_count")
    private int reactionCount = 0;

    @Column(name = "follower_count")
    private int followerCount = 0;


    @Builder
    public User(String oauthId, String name, String email,String nickname, String telNumber) {
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.telNumber = telNumber;
    }


    public void setRole(Role role) {
        this.role = role;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
    public void setScrapCount(int scrapCount) {
        this.scrapCount = scrapCount;
    }
    public void setReactionCount(int reactionCount) {
        this.reactionCount = reactionCount;
    }
    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }
}


