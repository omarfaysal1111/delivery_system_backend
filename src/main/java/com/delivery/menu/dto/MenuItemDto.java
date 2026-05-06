package com.delivery.menu.dto;

import com.delivery.menu.domain.MenuItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class MenuItemDto {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean isAvailable;

    public static MenuItemDto from(MenuItem i) {
        return MenuItemDto.builder()
                .id(i.getId()).categoryId(i.getCategoryId())
                .name(i.getName()).description(i.getDescription())
                .price(i.getPrice()).imageUrl(i.getImageUrl())
                .isAvailable(i.isAvailable())
                .build();
    }
}
