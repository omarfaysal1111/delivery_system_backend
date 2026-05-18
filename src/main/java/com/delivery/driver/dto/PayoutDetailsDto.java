package com.delivery.driver.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayoutDetailsDto {
    private String bankName;
    private String accountNumber;
    private String accountHolder;
}
