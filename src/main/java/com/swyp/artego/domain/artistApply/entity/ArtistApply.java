package com.swyp.artego.domain.artistApply.entity;


import com.swyp.artego.domain.artistApply.enums.Status;
import com.swyp.artego.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "artist_apply")
public class ArtistApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_apply_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // 유저는 반드시 있어야 하므로 nullable = false 권장
    private User user;

    @Column(name = "artist_about_me", nullable = false, length = 3000)
    private String artistAboutMe = ""; // Nullable // 자기소개

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "sns_link", nullable = false, length = 50)
    private String snsLink;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "rejected_count", nullable = false)
    private int rejectedCount = 0;

    @Builder
    public ArtistApply(User user, String artistAboutMe, String email, String snsLink) {
        this.user = user;
        this.artistAboutMe = artistAboutMe;
        this.email = email;
        this.snsLink = snsLink;
        this.status = Status.PENDING;
        this.rejectedCount = 0;
    }


}
