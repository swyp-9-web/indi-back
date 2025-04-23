package com.swyp.artego.domain.user.repository;

import com.swyp.artego.domain.user.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRepository extends JpaRepository<Commission, Long> {
}
