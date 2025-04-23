package com.swyp.artego.domain.post.repository;

import com.swyp.artego.domain.post.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
