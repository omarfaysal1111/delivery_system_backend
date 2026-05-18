package com.delivery.offer.service;

import com.delivery.offer.domain.Offer;
import com.delivery.offer.dto.CreateOfferRequest;
import com.delivery.offer.dto.OfferDto;
import com.delivery.offer.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;

    public List<OfferDto> getOffers(UUID restaurantId) {
        return offerRepository.findAllByRestaurantIdAndIsActiveTrue(restaurantId)
                .stream().map(OfferDto::from).toList();
    }

    @Transactional
    public OfferDto createOffer(UUID restaurantId, CreateOfferRequest req) {
        Offer offer = Offer.builder()
                .restaurantId(restaurantId)
                .title(req.getTitle())
                .discountPercent(req.getDiscountPercent())
                .minOrderAmount(req.getMinOrderAmount())
                .description(req.getDescription())
                .expiresAt(req.getExpiresAt())
                .build();
        return OfferDto.from(offerRepository.save(offer));
    }
}
