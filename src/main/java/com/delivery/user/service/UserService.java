package com.delivery.user.service;

import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.user.domain.User;
import com.delivery.user.dto.UpdateUserProfileRequest;
import com.delivery.user.dto.UserProfileDto;
import com.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateUserProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        return toDto(userRepository.save(user));
    }

    private UserProfileDto toDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }
}
