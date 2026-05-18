package com.delivery.banner.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateBannerRequest {
    private String imageUrl;
    private String title;
    private String subtitle;
    private String ctaText;
    private String discountText;
    private String deepLink;
    private boolean isActive;
    private int sortOrder;
    private LocalDateTime expiresAt;
}
