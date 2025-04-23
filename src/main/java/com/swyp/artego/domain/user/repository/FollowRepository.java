package com.swyp.artego.domain.user.repository;

import com.swyp.artego.domain.user.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}
