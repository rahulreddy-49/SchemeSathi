package com.schemesathi.controller;

import com.schemesathi.dto.ApiResponse;
import com.schemesathi.dto.SchemeApplicationDto;
import com.schemesathi.entity.User;
import com.schemesathi.repository.UserRepository;
import com.schemesathi.service.SchemeApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class SchemeApplicationController {

    private final SchemeApplicationService applicationService;
    private final UserRepository userRepository;

    public SchemeApplicationController(SchemeApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    private User resolveUser(Object principal) {
        if (principal instanceof User u) {
            return u;
        } else if (principal instanceof UserDetails ud) {
            return userRepository.findByEmail(ud.getUsername()).orElse(null);
        } else if (principal instanceof String email) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchemeApplicationDto>>> getApplications(
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        List<SchemeApplicationDto> list = applicationService.getApplications(user);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SchemeApplicationDto.ApplicationSummaryDto>> getSummary(
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        SchemeApplicationDto.ApplicationSummaryDto summary = applicationService.getApplicationSummary(user);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemeApplicationDto>> getApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            SchemeApplicationDto dto = applicationService.getApplicationById(user, id);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchemeApplicationDto>> createApplication(
            @Valid @RequestBody SchemeApplicationDto.CreateApplicationRequest req,
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            SchemeApplicationDto created = applicationService.createApplication(user, req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Scheme application tracking record created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemeApplicationDto>> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody SchemeApplicationDto.UpdateApplicationRequest req,
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            SchemeApplicationDto updated = applicationService.updateApplication(user, id, req);
            return ResponseEntity.ok(ApiResponse.success(updated, "Application tracking record updated successfully"));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(msg));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(msg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            applicationService.deleteApplication(user, id);
            return ResponseEntity.ok(ApiResponse.success(null, "Application tracking record removed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/scheme/{schemeId}")
    public ResponseEntity<ApiResponse<List<SchemeApplicationDto>>> getApplicationsForScheme(
            @PathVariable Long schemeId,
            @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        List<SchemeApplicationDto> list = applicationService.getApplicationsForScheme(user, schemeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
