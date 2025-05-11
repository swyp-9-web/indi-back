package com.swyp.artego.domain.comment.repository;

import com.swyp.artego.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryRepository {

    // TODO: line 22, c.createdAt ASC 를 사용하면 정렬이 제대로 안되는 문제가 있음.
    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.item.id = :itemId
            ORDER BY
                COALESCE(c.parent.id, c.id) DESC,
                CASE
                    WHEN c.parent IS NULL THEN 0
                    ELSE 1
                END,
                c.id ASC
            """)
    List<Comment> findByItemIdOrderByCreatedAtDesc(@Param("itemId") Long itemId);

    Comment findByParentId(@Param("parentId") Long parentId);

    List<Comment> findAllByOrderByCreatedAtDesc();
}
