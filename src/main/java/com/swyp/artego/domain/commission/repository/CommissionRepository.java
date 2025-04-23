package com.swyp.artego.domain.commission.repository;

import com.swyp.artego.domain.commission.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRepository extends JpaRepository<Commission, Long> {
}
