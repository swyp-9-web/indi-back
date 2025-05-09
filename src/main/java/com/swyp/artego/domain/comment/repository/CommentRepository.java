package com.swyp.artego.domain.comment.repository;

import com.swyp.artego.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> ,CommentQueryRepository{

    List<Comment> findByItemIdOrderByCreatedAtDesc(@Param("itemId") Long itemId);

    Comment findByParentId(@Param("parentId") Long parentId);

    List<Comment> findAllByOrderByCreatedAtDesc();
}
