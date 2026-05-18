package com.delivery.banner.dto;

import com.delivery.banner.domain.Banner;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class BannerDto {
    private UUID id;
    private String imageUrl;
    private String title;
    private String subtitle;
    private String ctaText;
    private String discountText;
    private String deepLink;
    private boolean isActive;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static BannerDto from(Banner b) {
        return BannerDto.builder()
                .id(b.getId()).imageUrl(b.getImageUrl()).title(b.getTitle())
                .subtitle(b.getSubtitle()).ctaText(b.getCtaText())
                .discountText(b.getDiscountText()).deepLink(b.getDeepLink())
                .isActive(b.isActive()).sortOrder(b.getSortOrder())
                .createdAt(b.getCreatedAt()).expiresAt(b.getExpiresAt())
                .build();
    }
}
