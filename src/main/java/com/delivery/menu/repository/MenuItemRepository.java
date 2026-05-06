package com.delivery.menu.repository;

import com.delivery.menu.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findAllByCategoryId(UUID categoryId);
    List<MenuItem> findAllByCategoryIdAndIsAvailableTrue(UUID categoryId);
}
