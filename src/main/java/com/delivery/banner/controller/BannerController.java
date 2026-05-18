package com.delivery.banner.controller;

import com.delivery.banner.dto.BannerDto;
import com.delivery.banner.dto.CreateBannerRequest;
import com.delivery.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/api/v1/banners")
    public ResponseEntity<List<BannerDto>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    @PostMapping("/api/v1/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BannerDto> create(@RequestBody CreateBannerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.create(req));
    }
}
