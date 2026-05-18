package com.delivery.auth.service;

import com.delivery.auth.domain.OtpRecord;
import com.delivery.auth.dto.AuthResponse;
import com.delivery.auth.jwt.JwtTokenProvider;
import com.delivery.auth.repository.OtpRecordRepository;
import com.delivery.common.exception.AuthException;
import com.delivery.user.domain.AuthProvider;
import com.delivery.user.domain.Role;
import com.delivery.user.domain.User;
import com.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRecordRepository otpRecordRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${app.otp.hash-codes:false}")
    private boolean hashCodes;

    @Transactional
    public void sendOtp(String phone, String role) {
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        OtpRecord record = OtpRecord.builder()
                .phone(phone)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();
        otpRecordRepository.save(record);

        log.info("[OTP] {} → {}", phone, code);

        if (!userRepository.existsByPhone(phone)) {
            User newUser = User.builder()
                    .phone(phone)
                    .email(phone + "@otp.local")
                    .name(phone)
                    .role(Role.valueOf(role))
                    .provider(AuthProvider.LOCAL)
                    .active(false)
                    .build();
            userRepository.save(newUser);
        }
    }

    @Transactional
    public AuthResponse verifyOtp(String phone, String otp) {
        OtpRecord record = otpRecordRepository
                .findTopByPhoneAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(phone, LocalDateTime.now())
                .orElseThrow(() -> new AuthException("No valid OTP found for this phone number"));

        if (!record.getCode().equals(otp)) {
            throw new AuthException("Invalid OTP code");
        }

        record.setUsed(true);
        otpRecordRepository.save(record);

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthException("User not found for phone: " + phone));

        user.setActive(true);
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.create(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
