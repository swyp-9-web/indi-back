package com.swyp.artego.domain.artistApply.repository;

import com.swyp.artego.domain.artistApply.entity.ArtistApply;
import com.swyp.artego.domain.artistApply.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistApplyRepository extends JpaRepository<ArtistApply, Long> {

//    boolean existsByUserIdAndStatus(Long userId, Status status);

    Long countAllByUserIdAndStatus(Long userId, Status status);

    List<ArtistApply> findAllByOrderByCreatedAtDesc();
}