package com.schemesathi.controller;

import com.schemesathi.dto.ApiResponse;
import com.schemesathi.dto.EligibilityResultDto;
import com.schemesathi.dto.HouseholdMemberDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.HouseholdMember;
import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.User;
import com.schemesathi.repository.SchemeRepository;
import com.schemesathi.service.EligibilityEngine;
import com.schemesathi.service.HouseholdMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household")
@SuppressWarnings("null")
public class HouseholdMemberController {

    private final HouseholdMemberService householdMemberService;
    private final EligibilityEngine eligibilityEngine;
    private final SchemeRepository schemeRepository;

    public HouseholdMemberController(HouseholdMemberService householdMemberService,
                                     EligibilityEngine eligibilityEngine,
                                     SchemeRepository schemeRepository) {
        this.householdMemberService = householdMemberService;
        this.eligibilityEngine = eligibilityEngine;
        this.schemeRepository = schemeRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HouseholdMemberDto>>> getHouseholdMembers(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        List<HouseholdMemberDto> members = householdMemberService.getHouseholdMembers(user);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseholdMemberDto>> getHouseholdMember(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            HouseholdMemberDto member = householdMemberService.getHouseholdMember(user, id);
            return ResponseEntity.ok(ApiResponse.success(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HouseholdMemberDto>> createHouseholdMember(
            @Valid @RequestBody HouseholdMemberDto dto,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            HouseholdMemberDto created = householdMemberService.createHouseholdMember(user, dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Household member added successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseholdMemberDto>> updateHouseholdMember(
            @PathVariable Long id,
            @Valid @RequestBody HouseholdMemberDto dto,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            HouseholdMemberDto updated = householdMemberService.updateHouseholdMember(user, id, dto);
            return ResponseEntity.ok(ApiResponse.success(updated, "Household member updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHouseholdMember(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            householdMemberService.deleteHouseholdMember(user, id);
            return ResponseEntity.ok(ApiResponse.success("Deleted", "Household member removed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/eligibility/all")
    public ResponseEntity<ApiResponse<List<EligibilityResultDto>>> evaluateAllForMember(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            HouseholdMember member = householdMemberService.getMemberEntity(user, id);
            CitizenProfile profile = member.toCitizenProfile();
            List<EligibilityResultDto> results = eligibilityEngine.evaluateAll(profile);
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/eligibility/{schemeId}")
    public ResponseEntity<ApiResponse<EligibilityResultDto>> evaluateSchemeForMember(
            @PathVariable Long id,
            @PathVariable Long schemeId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            HouseholdMember member = householdMemberService.getMemberEntity(user, id);
            Scheme scheme = schemeRepository.findById(schemeId).orElse(null);
            if (scheme == null) {
                return ResponseEntity.notFound().build();
            }
            CitizenProfile profile = member.toCitizenProfile();
            EligibilityResultDto result = eligibilityEngine.evaluate(scheme, profile);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
