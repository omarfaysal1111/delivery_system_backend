package com.delivery.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Pattern(regexp = "ROLE_CUSTOMER|ROLE_DRIVER|ROLE_RESTAURANT_OWNER",
             message = "must be ROLE_CUSTOMER, ROLE_DRIVER, or ROLE_RESTAURANT_OWNER")
    private String role;
}
