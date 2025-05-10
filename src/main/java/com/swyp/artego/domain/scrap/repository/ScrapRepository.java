package com.swyp.artego.domain.scrap.repository;

import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.scrap.entity.Scrap;
import com.swyp.artego.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    Optional<Scrap> findByUserAndItem(User user, Item item);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
