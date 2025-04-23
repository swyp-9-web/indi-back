package com.swyp.artego.domain.code.commission.repository;

import com.swyp.artego.domain.code.commission.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRepository extends JpaRepository<Commission, Long> {
}
