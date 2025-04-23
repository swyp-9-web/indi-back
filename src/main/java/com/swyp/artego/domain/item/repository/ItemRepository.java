package com.swyp.artego.domain.item.repository;

import com.swyp.artego.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
