package com.delivery.address.controller;

import com.delivery.address.dto.AddressDto;
import com.delivery.address.dto.CreateAddressRequest;
import com.delivery.address.service.AddressService;
import com.delivery.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(addressService.getAddresses(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<AddressDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(principal.getId(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody CreateAddressRequest req) {
        return ResponseEntity.ok(addressService.updateAddress(principal.getId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        addressService.deleteAddress(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressDto> setDefault(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(addressService.setDefault(principal.getId(), id));
    }
}
