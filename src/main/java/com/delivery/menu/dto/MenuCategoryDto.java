package com.delivery.menu.dto;

import com.delivery.menu.domain.MenuCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MenuCategoryDto {
    private UUID id;
    private UUID branchId;
    private String name;
    private int sortOrder;
    private boolean isVisible;
    private List<MenuItemDto> items;

    public static MenuCategoryDto from(MenuCategory c) {
        return MenuCategoryDto.builder()
                .id(c.getId()).branchId(c.getBranchId())
                .name(c.getName()).sortOrder(c.getSortOrder())
                .isVisible(c.isVisible())
                .build();
    }
}
