package com.swyp.artego.domain.user.repository;

import com.swyp.artego.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByOauthId(String oauthId);
    boolean existsByNickname(String nickname);


    @Modifying
    @Query("UPDATE User u SET u.scrapCount = u.scrapCount + :delta WHERE u.id = :userId")
    void incrementUserScrapCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE User u SET u.reactionCount = u.reactionCount + :delta WHERE u.id = :userId")
    void incrementUserReactionCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE User u SET u.followerCount = u.followerCount + :delta WHERE u.id = :userId")
    void incrementFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE User u SET u.itemCount = u.itemCount + :delta WHERE u.id = :userId")
    void incrementItemCount(@Param("userId") Long userId, @Param("delta") int delta);

}
