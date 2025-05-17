package com.swyp.artego.domain.item.repository;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> , ItemSearchRepository {

    /**
     * 모든 게시글을 생성일 기준으로 최신순 조회
     *
     * @return 최신순 정렬된 Item 리스트
     */
    List<Item> findAllByOrderByCreatedAtDesc();

    List<Item> findByStatusType(StatusType statusType);

    @Modifying
    @Query("UPDATE Item i SET i.scrapCount = i.scrapCount + :delta WHERE i.id = :itemId")
    void incrementScrapCount(@Param("itemId") Long itemId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Item i SET i.totalReactionCount = i.totalReactionCount + :delta WHERE i.id = :itemId")
    void incrementReactionCount(@Param("itemId") Long itemId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Item i SET i.likeCount = i.likeCount + :delta WHERE i.id = :itemId")
    void incrementLikeCount(@Param("itemId") Long itemId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Item i SET i.wantCount = i.wantCount + :delta WHERE i.id = :itemId")
    void incrementWantCount(@Param("itemId") Long itemId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Item i SET i.revisitCount = i.revisitCount + :delta WHERE i.id = :itemId")
    void incrementRevisitCount(@Param("itemId") Long itemId, @Param("delta") int delta);


}


