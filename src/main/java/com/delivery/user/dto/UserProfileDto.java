package com.delivery.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserProfileDto {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String avatar;
}
