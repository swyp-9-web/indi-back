package com.swyp.artego.domain.code.repository;

import com.swyp.artego.domain.code.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepository extends JpaRepository<Code, Long> {
}
