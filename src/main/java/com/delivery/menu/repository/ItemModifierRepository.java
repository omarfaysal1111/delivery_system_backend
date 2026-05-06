package com.delivery.menu.repository;

import com.delivery.menu.domain.ItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemModifierRepository extends JpaRepository<ItemModifier, UUID> {
    List<ItemModifier> findAllByItemId(UUID itemId);
    void deleteAllByItemId(UUID itemId);
}
