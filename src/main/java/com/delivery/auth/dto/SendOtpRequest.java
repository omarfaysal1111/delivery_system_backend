package com.delivery.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {
    @NotBlank
    @Pattern(regexp = "\\+?[0-9]{10,15}")
    private String phone;

    @NotBlank
    @Pattern(regexp = "ROLE_CUSTOMER|ROLE_DRIVER")
    private String role;
}
