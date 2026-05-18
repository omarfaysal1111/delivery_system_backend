package com.delivery.address.repository;

import com.delivery.address.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findAllByUserId(UUID userId);
}
