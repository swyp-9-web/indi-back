package com.swyp.artego.domain.follow.repository;

import com.swyp.artego.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    /**
     * 모든 팔로우를 생성일 기준으로 최신순 조회
     *
     * @return 최신순 정렬된 Follow 리스트
     */
    List<Follow> findAllByOrderByCreatedAtDesc();

}
