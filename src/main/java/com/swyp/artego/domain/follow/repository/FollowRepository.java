package com.swyp.artego.domain.follow.repository;

import com.swyp.artego.domain.follow.entity.Follow;
import com.swyp.artego.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowSearchRepository {
    /**
     * 모든 팔로우를 생성일 기준으로 최신순 조회
     *
     * @return 최신순 정렬된 Follow 리스트
     */
    List<Follow> findAllByOrderByCreatedAtDesc();

    Optional<Follow> findByUserAndUserArtist(User user, User userArtist);

    List<Follow> findTop5ByUserOrderByCreatedAtDesc(User user);

    int countByUser(User user);

}
