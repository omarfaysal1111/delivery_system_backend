package com.delivery.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateMenuCategoryRequest {
    @NotNull  private UUID branchId;
    @NotBlank private String name;
    private int sortOrder;
}
