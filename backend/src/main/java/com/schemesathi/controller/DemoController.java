package com.schemesathi.controller;

import com.schemesathi.dto.ApiResponse;
import com.schemesathi.dto.EligibilityResultDto;
import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.dto.SchemeDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.Scheme;
import com.schemesathi.repository.SchemeRepository;
import com.schemesathi.service.CitizenProfileService;
import com.schemesathi.service.EligibilityEngine;
import com.schemesathi.service.SchemeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final SchemeService schemeService;
    private final SchemeRepository schemeRepository;
    private final EligibilityEngine eligibilityEngine;

    public DemoController(SchemeService schemeService,
                          SchemeRepository schemeRepository,
                          EligibilityEngine eligibilityEngine) {
        this.schemeService = schemeService;
        this.schemeRepository = schemeRepository;
        this.eligibilityEngine = eligibilityEngine;
    }

    @GetMapping("/schemes")
    public ResponseEntity<ApiResponse<List<SchemeDto>>> getDemoSchemes() {
        return ResponseEntity.ok(ApiResponse.success(schemeService.getAllSchemes(null)));
    }

    @GetMapping("/schemes/{id}")
    public ResponseEntity<ApiResponse<SchemeDto>> getDemoScheme(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(schemeService.getSchemeById(id, null)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/eligibility/{schemeId}")
    public ResponseEntity<ApiResponse<EligibilityResultDto>> checkDemoEligibility(
            @PathVariable Long schemeId,
            @Valid @RequestBody CitizenProfileDto profileDto) {
        Scheme scheme = schemeRepository.findPublishedById(schemeId).orElse(null);
        if (scheme == null) {
            return ResponseEntity.notFound().build();
        }

        // Create transient detached profile from DTO with all demographic/socioeconomic fields
        CitizenProfile demoProfile = CitizenProfileService.toTransientEntity(profileDto);
        EligibilityResultDto result = eligibilityEngine.evaluate(scheme, demoProfile);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/eligibility/all")
    public ResponseEntity<ApiResponse<List<EligibilityResultDto>>> checkDemoAllEligibility(
            @Valid @RequestBody(required = false) CitizenProfileDto profileDto) {
        CitizenProfile demoProfile = CitizenProfileService.toTransientEntity(profileDto);
        List<EligibilityResultDto> results = eligibilityEngine.evaluateAll(demoProfile);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/profiles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDemoProfiles() {
        List<Map<String, Object>> demoProfiles = List.of(
            Map.of(
                "name", "Demo: Lakshmi Devi (Farmer)",
                "description", "35-year-old female farmer from rural Telangana",
                "isDemo", true,
                "profile", Map.ofEntries(
                    Map.entry("age", 35),
                    Map.entry("gender", "Female"),
                    Map.entry("state", "Telangana"),
                    Map.entry("occupation", "Farmer"),
                    Map.entry("annualIncome", 180000.0),
                    Map.entry("category", "OBC"),
                    Map.entry("educationLevel", "10th"),
                    Map.entry("landOwnership", true),
                    Map.entry("landArea", 2.5),
                    Map.entry("employmentStatus", "SelfEmployed"),
                    Map.entry("residenceType", "Rural"),
                    Map.entry("bplStatus", false),
                    Map.entry("familySize", 5)
                )
            ),
            Map.of(
                "name", "Demo: Ravi Kumar (Student)",
                "description", "20-year-old SC male student from Rajasthan",
                "isDemo", true,
                "profile", Map.ofEntries(
                    Map.entry("age", 20),
                    Map.entry("gender", "Male"),
                    Map.entry("state", "Rajasthan"),
                    Map.entry("occupation", "Student"),
                    Map.entry("annualIncome", 200000.0),
                    Map.entry("category", "SC"),
                    Map.entry("educationLevel", "Graduate"),
                    Map.entry("landOwnership", false),
                    Map.entry("employmentStatus", "Student"),
                    Map.entry("residenceType", "Urban"),
                    Map.entry("bplStatus", true),
                    Map.entry("familySize", 4)
                )
            ),
            Map.of(
                "name", "Demo: Meena Sharma (Urban Professional)",
                "description", "28-year-old female professional from Maharashtra",
                "isDemo", true,
                "profile", Map.ofEntries(
                    Map.entry("age", 28),
                    Map.entry("gender", "Female"),
                    Map.entry("state", "Maharashtra"),
                    Map.entry("occupation", "IT Professional"),
                    Map.entry("annualIncome", 600000.0),
                    Map.entry("category", "General"),
                    Map.entry("educationLevel", "PostGraduate"),
                    Map.entry("landOwnership", false),
                    Map.entry("employmentStatus", "Employed"),
                    Map.entry("residenceType", "Urban"),
                    Map.entry("bplStatus", false),
                    Map.entry("familySize", 3)
                )
            )
        );
        return ResponseEntity.ok(ApiResponse.success(demoProfiles));
    }
}
