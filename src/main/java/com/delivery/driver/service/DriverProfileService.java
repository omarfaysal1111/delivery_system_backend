package com.delivery.driver.service;

import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.driver.dto.DriverProfileDto;
import com.delivery.driver.dto.DriverRegisterRequest;
import com.delivery.driver.dto.UpdateDriverProfileRequest;
import com.delivery.user.domain.DriverProfile;
import com.delivery.user.domain.DriverStatus;
import com.delivery.user.domain.User;
import com.delivery.user.repository.DriverProfileRepository;
import com.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public DriverProfileDto register(UUID userId, DriverRegisterRequest req) {
        DriverProfile dp = driverProfileRepository.findByUserId(userId)
                .orElseGet(() -> DriverProfile.builder().userId(userId).build());
        dp.setVehicleType(req.getVehicleType());
        dp.setLicensePlate(req.getVehicleNumber());
        dp.setNationalId(req.getNationalId());
        dp.setStatus(DriverStatus.PENDING);
        DriverProfile saved = driverProfileRepository.save(dp);
        User user = getUser(userId);
        return DriverProfileDto.from(saved, user.getName(), user.getPhone());
    }

    public DriverProfileDto getProfile(UUID userId) {
        DriverProfile dp = getDriverProfile(userId);
        User user = getUser(userId);
        return DriverProfileDto.from(dp, user.getName(), user.getPhone());
    }

    @Transactional
    public DriverProfileDto updateProfile(UUID userId, UpdateDriverProfileRequest req) {
        DriverProfile dp = getDriverProfile(userId);
        if (req.getVehicleType() != null) dp.setVehicleType(req.getVehicleType());
        if (req.getVehicleNumber() != null) dp.setLicensePlate(req.getVehicleNumber());
        if (req.getProfilePhotoUrl() != null) dp.setProfilePhotoUrl(req.getProfilePhotoUrl());
        DriverProfile saved = driverProfileRepository.save(dp);
        User user = getUser(userId);
        return DriverProfileDto.from(saved, user.getName(), user.getPhone());
    }

    @Transactional
    public PayoutDetailsResult getPayoutDetails(UUID userId) {
        DriverProfile dp = getDriverProfile(userId);
        return new PayoutDetailsResult(dp.getBankName(), dp.getBankAccountNumber(), dp.getBankAccountHolder());
    }

    @Transactional
    public PayoutDetailsResult updatePayoutDetails(UUID userId, String bankName,
                                                    String accountNumber, String accountHolder) {
        DriverProfile dp = getDriverProfile(userId);
        dp.setBankName(bankName);
        dp.setBankAccountNumber(accountNumber);
        dp.setBankAccountHolder(accountHolder);
        driverProfileRepository.save(dp);
        return new PayoutDetailsResult(bankName, accountNumber, accountHolder);
    }

    public DriverProfile getDriverProfile(UUID userId) {
        return driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public record PayoutDetailsResult(String bankName, String accountNumber, String accountHolder) {}
}
