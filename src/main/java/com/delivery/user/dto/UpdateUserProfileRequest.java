package com.delivery.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {
    private String name;
    private String phone;
    private String email;
    private String avatar;
}
