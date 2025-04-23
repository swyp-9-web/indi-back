package com.swyp.artego.domain.post.repository;

import com.swyp.artego.domain.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
