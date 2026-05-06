package com.delivery.menu.repository;

import com.delivery.menu.domain.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
    List<MenuCategory> findAllByBranchIdAndIsVisibleTrueOrderBySortOrderAsc(UUID branchId);
    List<MenuCategory> findAllByBranchIdOrderBySortOrderAsc(UUID branchId);
}
