package com.schemesathi.controller;

import com.schemesathi.dto.ApiResponse;
import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.entity.User;
import com.schemesathi.service.CitizenProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CitizenProfileService profileService;

    public ProfileController(CitizenProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CitizenProfileDto>> getProfile(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }
        CitizenProfileDto profile = profileService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CitizenProfileDto>> saveProfile(
            @AuthenticationPrincipal User user,
            @RequestBody CitizenProfileDto dto) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }
        CitizenProfileDto profile = profileService.saveProfile(user, dto);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile saved successfully"));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<CitizenProfileDto>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody CitizenProfileDto dto) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }
        CitizenProfileDto profile = profileService.updateProfileFields(user, dto);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
    }
}
