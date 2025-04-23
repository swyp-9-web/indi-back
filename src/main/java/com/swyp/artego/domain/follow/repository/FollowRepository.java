package com.swyp.artego.domain.follow.repository;

import com.swyp.artego.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}
