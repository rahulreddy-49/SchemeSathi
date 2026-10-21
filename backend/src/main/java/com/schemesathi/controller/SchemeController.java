package com.schemesathi.controller;

import com.schemesathi.dto.ApiResponse;
import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.dto.EligibilityResultDto;
import com.schemesathi.dto.SchemeDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.SavedScheme;
import com.schemesathi.entity.User;
import com.schemesathi.repository.SchemeRepository;
import jakarta.validation.Valid;
import com.schemesathi.repository.SavedSchemeRepository;
import com.schemesathi.service.CitizenProfileService;
import com.schemesathi.service.EligibilityEngine;
import com.schemesathi.service.SchemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schemes")
@SuppressWarnings("null")
public class SchemeController {

    private final SchemeService schemeService;
    private final SchemeRepository schemeRepository;
    private final SavedSchemeRepository savedSchemeRepository;
    private final EligibilityEngine eligibilityEngine;
    private final CitizenProfileService profileService;
    private final com.schemesathi.repository.HouseholdMemberRepository householdMemberRepository;

    public SchemeController(SchemeService schemeService,
                            SchemeRepository schemeRepository,
                            SavedSchemeRepository savedSchemeRepository,
                            EligibilityEngine eligibilityEngine,
                            CitizenProfileService profileService,
                            com.schemesathi.repository.HouseholdMemberRepository householdMemberRepository) {
        this.schemeService = schemeService;
        this.schemeRepository = schemeRepository;
        this.savedSchemeRepository = savedSchemeRepository;
        this.eligibilityEngine = eligibilityEngine;
        this.profileService = profileService;
        this.householdMemberRepository = householdMemberRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchemeDto>>> getAllSchemes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state,
            @AuthenticationPrincipal User user) {
        Long userId = user != null ? user.getId() : null;

        List<SchemeDto> schemes;
        if (search != null && !search.isEmpty()) {
            schemes = schemeService.searchSchemes(search, userId);
        } else if (category != null && state != null) {
            schemes = schemeService.getSchemesByCategoryAndState(category, state, userId);
        } else if (category != null) {
            schemes = schemeService.getSchemesByCategory(category, userId);
        } else if (state != null) {
            schemes = schemeService.getApplicableSchemes(state, userId);
        } else {
            schemes = schemeService.getAllSchemes(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(schemes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemeDto>> getScheme(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            Long userId = user != null ? user.getId() : null;
            SchemeDto scheme = schemeService.getSchemeById(id, userId);
            return ResponseEntity.ok(ApiResponse.success(scheme));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/eligibility")
    public ResponseEntity<ApiResponse<EligibilityResultDto>> checkEligibility(
            @PathVariable Long id,
            @RequestParam(required = false) Long memberId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        Scheme scheme = schemeRepository.findById(id).orElse(null);
        if (scheme == null) {
            return ResponseEntity.notFound().build();
        }

        CitizenProfile profile;
        if (memberId != null) {
            com.schemesathi.entity.HouseholdMember member = householdMemberRepository.findByIdAndUserId(memberId, user.getId())
                    .orElse(null);
            if (member == null) {
                return ResponseEntity.notFound().build();
            }
            profile = member.toCitizenProfile();
        } else {
            profile = profileService.getProfileEntity(user);
        }

        EligibilityResultDto result = eligibilityEngine.evaluate(scheme, profile);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/eligibility/all")
    public ResponseEntity<ApiResponse<List<EligibilityResultDto>>> checkAllEligibility(
            @RequestParam(required = false) Long memberId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        CitizenProfile profile;
        if (memberId != null) {
            com.schemesathi.entity.HouseholdMember member = householdMemberRepository.findByIdAndUserId(memberId, user.getId())
                    .orElse(null);
            if (member == null) {
                return ResponseEntity.notFound().build();
            }
            profile = member.toCitizenProfile();
        } else {
            profile = profileService.getProfileEntity(user);
        }

        List<EligibilityResultDto> results = eligibilityEngine.evaluateAll(profile);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/{id}/eligibility")
    public ResponseEntity<ApiResponse<EligibilityResultDto>> evaluateSchemeWithCriteria(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CitizenProfileDto profileDto,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        Scheme scheme = schemeRepository.findById(id).orElse(null);
        if (scheme == null) {
            return ResponseEntity.notFound().build();
        }

        CitizenProfile profileToEvaluate = profileDto != null
                ? CitizenProfileService.toTransientEntity(profileDto)
                : profileService.getProfileEntity(user);

        EligibilityResultDto result = eligibilityEngine.evaluate(scheme, profileToEvaluate);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/eligibility/all")
    public ResponseEntity<ApiResponse<List<EligibilityResultDto>>> evaluateAllWithCriteria(
            @Valid @RequestBody(required = false) CitizenProfileDto profileDto,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        CitizenProfile profileToEvaluate = profileDto != null
                ? CitizenProfileService.toTransientEntity(profileDto)
                : profileService.getProfileEntity(user);

        List<EligibilityResultDto> results = eligibilityEngine.evaluateAll(profileToEvaluate);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/{id}/save")
    @Transactional
    public ResponseEntity<ApiResponse<String>> saveScheme(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        if (savedSchemeRepository.existsByUserIdAndSchemeId(user.getId(), id)) {
            return ResponseEntity.ok(ApiResponse.success("Already saved", "Scheme already saved"));
        }

        Scheme scheme = schemeRepository.findById(id).orElse(null);
        if (scheme == null) {
            return ResponseEntity.notFound().build();
        }

        SavedScheme saved = SavedScheme.builder().user(user).scheme(scheme).build();
        savedSchemeRepository.save(saved);
        return ResponseEntity.ok(ApiResponse.success("Saved", "Scheme saved successfully"));
    }

    @DeleteMapping("/{id}/save")
    @Transactional
    public ResponseEntity<ApiResponse<String>> unsaveScheme(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        savedSchemeRepository.deleteByUserIdAndSchemeId(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Removed", "Scheme removed from saved"));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<SchemeDto>>> getSavedSchemes(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        List<SavedScheme> saved = savedSchemeRepository.findByUserId(user.getId());
        List<SchemeDto> schemes = saved.stream()
                .map(s -> schemeService.getSchemeById(s.getScheme().getId(), user.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(schemes));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = List.of(
            "Agriculture", "Education", "Health", "Housing", "Employment",
            "Social Security", "Women Empowerment", "Insurance"
        );
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
