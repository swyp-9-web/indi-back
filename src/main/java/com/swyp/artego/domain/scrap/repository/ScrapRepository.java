package com.swyp.artego.domain.scrap.repository;

import com.swyp.artego.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
}
