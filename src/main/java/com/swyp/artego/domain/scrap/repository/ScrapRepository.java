package com.swyp.artego.domain.scrap.repository;

import com.swyp.artego.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    List<Scrap> findAllByOrderByCreatedAtDesc();
}
