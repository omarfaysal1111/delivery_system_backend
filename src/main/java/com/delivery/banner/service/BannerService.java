package com.delivery.banner.service;

import com.delivery.banner.domain.Banner;
import com.delivery.banner.dto.BannerDto;
import com.delivery.banner.dto.CreateBannerRequest;
import com.delivery.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Cacheable("banners")
    public List<BannerDto> getActiveBanners() {
        return bannerRepository.findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream().map(BannerDto::from).toList();
    }

    @Transactional
    @CacheEvict(value = "banners", allEntries = true)
    public BannerDto create(CreateBannerRequest req) {
        Banner banner = Banner.builder()
                .imageUrl(req.getImageUrl()).title(req.getTitle())
                .subtitle(req.getSubtitle()).ctaText(req.getCtaText())
                .discountText(req.getDiscountText()).deepLink(req.getDeepLink())
                .isActive(req.isActive()).sortOrder(req.getSortOrder())
                .expiresAt(req.getExpiresAt())
                .build();
        return BannerDto.from(bannerRepository.save(banner));
    }
}
