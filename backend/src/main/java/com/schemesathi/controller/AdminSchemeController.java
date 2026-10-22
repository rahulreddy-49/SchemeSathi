package com.schemesathi.controller;

import com.schemesathi.dto.AdminSchemeDto;
import com.schemesathi.dto.AdminSchemeStatsDto;
import com.schemesathi.dto.AdminVerificationRequestDto;
import com.schemesathi.dto.ApiResponse;
import com.schemesathi.entity.User;
import com.schemesathi.service.AdminSchemeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/schemes")
@SuppressWarnings("null")
public class AdminSchemeController {

    private final AdminSchemeService adminSchemeService;

    public AdminSchemeController(AdminSchemeService adminSchemeService) {
        this.adminSchemeService = adminSchemeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminSchemeDto>>> getSchemes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String publicationStatus,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String scope) {
        List<AdminSchemeDto> schemes = adminSchemeService.getSchemes(search, publicationStatus, verificationStatus, category, scope);
        return ResponseEntity.ok(ApiResponse.success(schemes));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminSchemeStatsDto>> getStats() {
        AdminSchemeStatsDto stats = adminSchemeService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminSchemeDto>> getSchemeById(@PathVariable Long id) {
        AdminSchemeDto scheme = adminSchemeService.getSchemeById(id);
        return ResponseEntity.ok(ApiResponse.success(scheme));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminSchemeDto>> createScheme(
            @Valid @RequestBody AdminSchemeDto dto,
            @AuthenticationPrincipal User user) {
        AdminSchemeDto created = adminSchemeService.createScheme(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Scheme created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminSchemeDto>> updateScheme(
            @PathVariable Long id,
            @Valid @RequestBody AdminSchemeDto dto,
            @AuthenticationPrincipal User user) {
        AdminSchemeDto updated = adminSchemeService.updateScheme(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success(updated, "Scheme updated successfully"));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<AdminSchemeDto>> verifyScheme(
            @PathVariable Long id,
            @Valid @RequestBody AdminVerificationRequestDto dto,
            @AuthenticationPrincipal User user) {
        AdminSchemeDto verified = adminSchemeService.verifyScheme(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success(verified, "Official source verification recorded successfully"));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<AdminSchemeDto>> publishScheme(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AdminSchemeDto published = adminSchemeService.publishScheme(id, user);
        return ResponseEntity.ok(ApiResponse.success(published, "Scheme published successfully"));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<AdminSchemeDto>> archiveScheme(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AdminSchemeDto archived = adminSchemeService.archiveScheme(id, user);
        return ResponseEntity.ok(ApiResponse.success(archived, "Scheme archived successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteScheme(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        adminSchemeService.deleteScheme(id, user);
        return ResponseEntity.ok(ApiResponse.success("Deleted", "Scheme deleted successfully"));
    }
}
