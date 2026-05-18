package com.delivery.offer.repository;

import com.delivery.offer.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findAllByRestaurantIdAndIsActiveTrue(UUID restaurantId);
}
