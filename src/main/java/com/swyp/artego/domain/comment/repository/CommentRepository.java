package com.swyp.artego.domain.comment.repository;

import com.swyp.artego.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryRepository {

    // TODO: line 19, 36, c.createdAt ASC 를 사용하면 정렬이 제대로 안되는 문제가 있음.
    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.item.id = :itemId AND c.parent IS NULL
            ORDER BY c.id DESC
            """)
    Page<Comment> findRootCommentsByItemId(@Param("itemId") Long itemId, Pageable pageable);

    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.parent.id IN :parentIds
            ORDER BY c.parent.id DESC, c.id ASC
            """)
    List<Comment> findChildCommentsByParentIds(@Param("parentIds") List<Long> parentIds);

    @Query("""
            SELECT COUNT(c)
            FROM Comment c
            WHERE c.item.id = :itemId AND c.parent IS NULL
            """)
    Long countRootCommentsByItemId(@Param("itemId") Long itemId);

    Long countAllByItemId(@Param("itemId") Long itemId);

    Comment findByParentId(@Param("parentId") Long parentId);

    List<Comment> findAllByOrderByCreatedAtDesc();
}
