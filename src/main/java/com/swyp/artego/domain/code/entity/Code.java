package com.swyp.artego.domain.code.entity;

import com.swyp.artego.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Code extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grp_cd", length = 16, nullable = false)
    private String grpCd;

    @Column(name = "cd", length = 16, nullable = false)
    private String cd;

    @Column(name = "grp_cd_nm", length = 50, nullable = false)
    private String grpCdNm;

    @Column(name = "cd_nm", length = 50, nullable = false)
    private String cdNm;
}
