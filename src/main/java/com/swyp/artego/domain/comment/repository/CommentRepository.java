package com.swyp.artego.domain.comment.repository;

import com.swyp.artego.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemIdOrderByCreatedAtDesc(@Param("itemId") Long itemId);

    List<Comment> findAllByOrderByCreatedAtDesc();
}
