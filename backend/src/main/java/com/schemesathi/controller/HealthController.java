package com.schemesathi.controller;

import com.schemesathi.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight, unauthenticated health check endpoint for container orchestrators (e.g. Render).
 * Returns minimal status without querying the database or exposing internal configuration.
 */
@RestController
@RequestMapping("/api/public")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "UP"), "SchemeSathi backend is running"));
    }
}
