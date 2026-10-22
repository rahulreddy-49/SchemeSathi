package com.schemesathi.service;

import com.schemesathi.dto.SchemeApplicationDto;
import com.schemesathi.entity.*;
import com.schemesathi.repository.HouseholdMemberRepository;
import com.schemesathi.repository.SchemeApplicationRepository;
import com.schemesathi.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class SchemeApplicationService {

    private final SchemeApplicationRepository applicationRepository;
    private final SchemeRepository schemeRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    private static final Collection<ApplicationStatus> TERMINAL_STATUSES = Set.of(
            ApplicationStatus.REJECTED,
            ApplicationStatus.CANCELLED,
            ApplicationStatus.COMPLETED
    );

    public SchemeApplicationService(
            SchemeApplicationRepository applicationRepository,
            SchemeRepository schemeRepository,
            HouseholdMemberRepository householdMemberRepository) {
        this.applicationRepository = applicationRepository;
        this.schemeRepository = schemeRepository;
        this.householdMemberRepository = householdMemberRepository;
    }

    @Transactional
    public SchemeApplicationDto createApplication(User user, SchemeApplicationDto.CreateApplicationRequest req) {
        if (req.getSchemeId() == null) {
            throw new IllegalArgumentException("Scheme ID is required");
        }

        Scheme scheme = schemeRepository.findPublishedById(req.getSchemeId())
                .orElseThrow(() -> new IllegalArgumentException("Scheme not found with id: " + req.getSchemeId()));

        HouseholdMember householdMember = null;
        String applicantType = "SELF";
        String applicantName = (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName()
                : user.getEmail();

        if (req.getHouseholdMemberId() != null) {
            householdMember = householdMemberRepository.findByIdAndUserId(req.getHouseholdMemberId(), user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Household member not found or does not belong to your account"));
            applicantType = "HOUSEHOLD_MEMBER";
            applicantName = householdMember.getFullName() + " (" + householdMember.getRelationship() + ")";
        }

        // Prevent duplicate active tracking records
        boolean alreadyActive;
        if (householdMember != null) {
            alreadyActive = applicationRepository.existsByUserIdAndSchemeIdAndHouseholdMemberIdAndStatusNotIn(
                    user.getId(), scheme.getId(), householdMember.getId(), TERMINAL_STATUSES);
        } else {
            alreadyActive = applicationRepository.existsByUserIdAndSchemeIdAndHouseholdMemberIsNullAndStatusNotIn(
                    user.getId(), scheme.getId(), TERMINAL_STATUSES);
        }

        if (alreadyActive) {
            throw new IllegalArgumentException("An active application tracking record already exists for " + applicantName + " under '" + scheme.getName() + "'.");
        }

        ApplicationStatus initialStatus = req.getStatus() != null ? req.getStatus() : ApplicationStatus.PLANNING;
        LocalDate appliedDate = req.getAppliedDate();
        if ((initialStatus == ApplicationStatus.SUBMITTED || initialStatus == ApplicationStatus.UNDER_REVIEW) && appliedDate == null) {
            appliedDate = LocalDate.now();
        }

        SchemeApplication application = SchemeApplication.builder()
                .user(user)
                .scheme(scheme)
                .householdMember(householdMember)
                .applicantType(applicantType)
                .applicantName(applicantName)
                .status(initialStatus)
                .applicationReference(req.getApplicationReference() != null ? req.getApplicationReference().trim() : null)
                .appliedDate(appliedDate)
                .nextActionDate(req.getNextActionDate())
                .nextActionNotes(req.getNextActionNotes() != null ? req.getNextActionNotes().trim() : null)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .build();

        String initNotes = initialStatus == ApplicationStatus.PLANNING
                ? "Application tracking initiated (Planning phase)"
                : "Application tracking initiated with status: " + initialStatus.getDisplayName();
        if (req.getNotes() != null && !req.getNotes().isBlank()) {
            initNotes += " — Note: " + req.getNotes().trim();
        }

        application.addHistory(null, initialStatus, initNotes, "CITIZEN");

        application = applicationRepository.save(application);
        return toDto(application, true);
    }

    @Transactional(readOnly = true)
    public List<SchemeApplicationDto> getApplications(User user) {
        return applicationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(app -> toDto(app, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SchemeApplicationDto getApplicationById(User user, Long id) {
        SchemeApplication app = applicationRepository.findByIdAndUserIdWithHistory(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Application tracking record not found with id: " + id));
        return toDto(app, true);
    }

    @Transactional
    public SchemeApplicationDto updateApplication(User user, Long id, SchemeApplicationDto.UpdateApplicationRequest req) {
        SchemeApplication app = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Application tracking record not found with id: " + id));

        // Status transition handling
        if (req.getStatus() != null && req.getStatus() != app.getStatus()) {
            ApplicationStatus oldStatus = app.getStatus();
            ApplicationStatus newStatus = req.getStatus();

            if (!oldStatus.isValidTransition(newStatus)) {
                throw new IllegalArgumentException("Cannot transition application status from '" 
                        + oldStatus.getDisplayName() + "' to '" + newStatus.getDisplayName() + "'.");
            }

            app.setStatus(newStatus);

            if (newStatus == ApplicationStatus.SUBMITTED && app.getAppliedDate() == null) {
                app.setAppliedDate(req.getAppliedDate() != null ? req.getAppliedDate() : LocalDate.now());
            }

            String changeNote = (req.getStatusChangeNote() != null && !req.getStatusChangeNote().isBlank())
                    ? req.getStatusChangeNote().trim()
                    : "Status updated to " + newStatus.getDisplayName();

            app.addHistory(oldStatus, newStatus, changeNote, "CITIZEN");
        }

        if (req.getApplicationReference() != null) {
            app.setApplicationReference(req.getApplicationReference().trim());
        }

        if (req.getAppliedDate() != null) {
            app.setAppliedDate(req.getAppliedDate());
        }

        if (req.getNextActionDate() != null) {
            app.setNextActionDate(req.getNextActionDate());
        }

        if (req.getNextActionNotes() != null) {
            app.setNextActionNotes(req.getNextActionNotes().trim());
        }

        if (req.getNotes() != null) {
            app.setNotes(req.getNotes().trim());
        }

        app = applicationRepository.save(app);
        return toDto(app, true);
    }

    @Transactional
    public void deleteApplication(User user, Long id) {
        SchemeApplication app = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Application tracking record not found with id: " + id));
        applicationRepository.delete(app);
    }

    @Transactional(readOnly = true)
    public SchemeApplicationDto.ApplicationSummaryDto getApplicationSummary(User user) {
        SchemeApplicationDto.ApplicationSummaryDto summary = new SchemeApplicationDto.ApplicationSummaryDto();
        summary.setTotalApplications(applicationRepository.countByUserId(user.getId()));
        summary.setPlanningCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.PLANNING));
        summary.setReadyToApplyCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.READY_TO_APPLY));
        summary.setSubmittedCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.SUBMITTED));
        summary.setUnderReviewCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.UNDER_REVIEW));
        summary.setActionRequiredCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.ACTION_REQUIRED));
        summary.setApprovedCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.APPROVED));
        summary.setRejectedCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.REJECTED));
        summary.setCompletedCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.COMPLETED));
        summary.setCancelledCount(applicationRepository.countByUserIdAndStatus(user.getId(), ApplicationStatus.CANCELLED));
        return summary;
    }

    @Transactional(readOnly = true)
    public List<SchemeApplicationDto> getApplicationsForScheme(User user, Long schemeId) {
        return applicationRepository.findByUserIdAndSchemeId(user.getId(), schemeId)
                .stream()
                .map(app -> toDto(app, false))
                .collect(Collectors.toList());
    }

    private SchemeApplicationDto toDto(SchemeApplication entity, boolean includeHistory) {
        SchemeApplicationDto dto = new SchemeApplicationDto();
        dto.setId(entity.getId());
        dto.setSchemeId(entity.getScheme().getId());
        dto.setSchemeName(entity.getScheme().getName());
        dto.setSchemeCategory(entity.getScheme().getBenefitCategory());
        dto.setGovernmentAuthority(entity.getScheme().getGovernmentAuthority());
        dto.setHouseholdMemberId(entity.getHouseholdMember() != null ? entity.getHouseholdMember().getId() : null);
        dto.setApplicantType(entity.getApplicantType());
        dto.setApplicantName(entity.getApplicantName());
        dto.setStatus(entity.getStatus());
        dto.setApplicationReference(entity.getApplicationReference());
        dto.setAppliedDate(entity.getAppliedDate());
        dto.setLastStatusUpdate(entity.getLastStatusUpdate());
        dto.setNextActionDate(entity.getNextActionDate());
        dto.setNextActionNotes(entity.getNextActionNotes());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (includeHistory && entity.getHistory() != null) {
            List<SchemeApplicationDto.SchemeApplicationHistoryDto> historyDtos = entity.getHistory().stream()
                    .map(h -> {
                        SchemeApplicationDto.SchemeApplicationHistoryDto hDto = new SchemeApplicationDto.SchemeApplicationHistoryDto();
                        hDto.setId(h.getId());
                        hDto.setFromStatus(h.getFromStatus());
                        hDto.setToStatus(h.getToStatus());
                        hDto.setNotes(h.getNotes());
                        hDto.setInitiatedBy(h.getInitiatedBy());
                        hDto.setCreatedAt(h.getCreatedAt());
                        return hDto;
                    })
                    .collect(Collectors.toList());
            dto.setHistory(historyDtos);
        }

        return dto;
    }
}
