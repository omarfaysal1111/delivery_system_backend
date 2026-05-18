package com.delivery.banner.repository;

import com.delivery.banner.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {
    List<Banner> findAllByIsActiveTrueOrderBySortOrderAsc();
}
