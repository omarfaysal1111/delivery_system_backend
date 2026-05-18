package com.delivery.user.controller;

import com.delivery.common.security.UserPrincipal;
import com.delivery.user.dto.UpdateUserProfileRequest;
import com.delivery.user.dto.UserProfileDto;
import com.delivery.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateUserProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), req));
    }
}
