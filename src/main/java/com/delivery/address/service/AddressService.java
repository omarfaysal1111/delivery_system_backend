package com.delivery.address.service;

import com.delivery.address.domain.UserAddress;
import com.delivery.address.dto.AddressDto;
import com.delivery.address.dto.CreateAddressRequest;
import com.delivery.address.repository.UserAddressRepository;
import com.delivery.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository addressRepository;

    public List<AddressDto> getAddresses(UUID userId) {
        return addressRepository.findAllByUserId(userId).stream()
                .map(AddressDto::from).toList();
    }

    @Transactional
    public AddressDto createAddress(UUID userId, CreateAddressRequest req) {
        UserAddress address = UserAddress.builder()
                .userId(userId)
                .label(req.getLabel())
                .fullAddress(req.getFullAddress())
                .lat(req.getLat())
                .lng(req.getLng())
                .city(req.getCity())
                .neighborhood(req.getNeighborhood())
                .isDefault(req.isDefault())
                .build();
        if (req.isDefault()) clearOtherDefaults(userId);
        return AddressDto.from(addressRepository.save(address));
    }

    @Transactional
    public AddressDto updateAddress(UUID userId, UUID addressId, CreateAddressRequest req) {
        UserAddress address = findOwned(userId, addressId);
        if (req.getLabel() != null) address.setLabel(req.getLabel());
        if (req.getFullAddress() != null) address.setFullAddress(req.getFullAddress());
        if (req.getLat() != null) address.setLat(req.getLat());
        if (req.getLng() != null) address.setLng(req.getLng());
        if (req.getCity() != null) address.setCity(req.getCity());
        if (req.getNeighborhood() != null) address.setNeighborhood(req.getNeighborhood());
        if (req.isDefault()) {
            clearOtherDefaults(userId);
            address.setDefault(true);
        }
        return AddressDto.from(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = findOwned(userId, addressId);
        addressRepository.delete(address);
    }

    @Transactional
    public AddressDto setDefault(UUID userId, UUID addressId) {
        clearOtherDefaults(userId);
        UserAddress address = findOwned(userId, addressId);
        address.setDefault(true);
        return AddressDto.from(addressRepository.save(address));
    }

    private void clearOtherDefaults(UUID userId) {
        addressRepository.findAllByUserId(userId).forEach(a -> {
            if (a.isDefault()) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        });
    }

    private UserAddress findOwned(UUID userId, UUID addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Address not found");
        }
        return address;
    }
}
