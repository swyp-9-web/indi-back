package com.swyp.artego.domain.item.repository;

import com.swyp.artego.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> , ItemSearchRepository {

    /**
     * 모든 게시글을 생성일 기준으로 최신순 조회
     *
     * @return 최신순 정렬된 Item 리스트
     */
    List<Item> findAllByOrderByCreatedAtDesc();
}


