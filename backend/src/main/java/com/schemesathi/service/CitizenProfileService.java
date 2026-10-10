package com.schemesathi.service;

import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.User;
import com.schemesathi.repository.CitizenProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class CitizenProfileService {

    private final CitizenProfileRepository profileRepository;

    public CitizenProfileService(CitizenProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public CitizenProfileDto getProfile(User user) {
        CitizenProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(null);
        if (profile == null) {
            return CitizenProfileDto.builder().completenessPercentage(0).build();
        }
        return toDto(profile);
    }

    @Transactional
    public CitizenProfileDto saveProfile(User user, CitizenProfileDto dto) {
        CitizenProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(CitizenProfile.builder().user(user).build());

        updateEntity(profile, dto);
        profile = profileRepository.save(profile);
        return toDto(profile);
    }

    @Transactional
    public CitizenProfileDto updateProfileFields(User user, CitizenProfileDto dto) {
        CitizenProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(CitizenProfile.builder().user(user).build());

        if (dto.getAge() != null) profile.setAge(dto.getAge());
        if (dto.getGender() != null) profile.setGender(dto.getGender());
        if (dto.getState() != null) profile.setState(dto.getState());
        if (dto.getDistrict() != null) profile.setDistrict(dto.getDistrict());
        if (dto.getOccupation() != null) profile.setOccupation(dto.getOccupation());
        if (dto.getAnnualIncome() != null) profile.setAnnualIncome(dto.getAnnualIncome());
        if (dto.getEducationLevel() != null) profile.setEducationLevel(dto.getEducationLevel());
        if (dto.getCategory() != null) profile.setCategory(dto.getCategory());
        if (dto.getDisabilityStatus() != null) profile.setDisabilityStatus(dto.getDisabilityStatus());
        if (dto.getDisabilityType() != null) profile.setDisabilityType(dto.getDisabilityType());
        if (dto.getDisabilityPercentage() != null) profile.setDisabilityPercentage(dto.getDisabilityPercentage());
        if (dto.getLandOwnership() != null) profile.setLandOwnership(dto.getLandOwnership());
        if (dto.getLandArea() != null) profile.setLandArea(dto.getLandArea());
        if (dto.getEmploymentStatus() != null) profile.setEmploymentStatus(dto.getEmploymentStatus());
        if (dto.getResidenceType() != null) profile.setResidenceType(dto.getResidenceType());
        if (dto.getMaritalStatus() != null) profile.setMaritalStatus(dto.getMaritalStatus());
        if (dto.getFamilySize() != null) profile.setFamilySize(dto.getFamilySize());
        if (dto.getBplStatus() != null) profile.setBplStatus(dto.getBplStatus());
        if (dto.getRationCardType() != null) profile.setRationCardType(dto.getRationCardType());
        if (dto.getMinorityStatus() != null) profile.setMinorityStatus(dto.getMinorityStatus());
        if (dto.getExServiceman() != null) profile.setExServiceman(dto.getExServiceman());

        profile = profileRepository.save(profile);
        return toDto(profile);
    }

    public CitizenProfile getProfileEntity(User user) {
        return profileRepository.findByUserId(user.getId()).orElse(null);
    }

    private void updateEntity(CitizenProfile profile, CitizenProfileDto dto) {
        profile.setAge(dto.getAge());
        profile.setGender(dto.getGender());
        profile.setState(dto.getState());
        profile.setDistrict(dto.getDistrict());
        profile.setOccupation(dto.getOccupation());
        profile.setAnnualIncome(dto.getAnnualIncome());
        profile.setEducationLevel(dto.getEducationLevel());
        profile.setCategory(dto.getCategory());
        profile.setDisabilityStatus(dto.getDisabilityStatus());
        profile.setDisabilityType(dto.getDisabilityType());
        profile.setDisabilityPercentage(dto.getDisabilityPercentage());
        profile.setLandOwnership(dto.getLandOwnership());
        profile.setLandArea(dto.getLandArea());
        profile.setEmploymentStatus(dto.getEmploymentStatus());
        profile.setResidenceType(dto.getResidenceType());
        profile.setMaritalStatus(dto.getMaritalStatus());
        profile.setFamilySize(dto.getFamilySize());
        profile.setBplStatus(dto.getBplStatus());
        profile.setRationCardType(dto.getRationCardType());
        profile.setMinorityStatus(dto.getMinorityStatus());
        profile.setExServiceman(dto.getExServiceman());
    }

    private CitizenProfileDto toDto(CitizenProfile p) {
        return CitizenProfileDto.builder()
                .id(p.getId())
                .age(p.getAge())
                .gender(p.getGender())
                .state(p.getState())
                .district(p.getDistrict())
                .occupation(p.getOccupation())
                .annualIncome(p.getAnnualIncome())
                .educationLevel(p.getEducationLevel())
                .category(p.getCategory())
                .disabilityStatus(p.getDisabilityStatus())
                .disabilityType(p.getDisabilityType())
                .disabilityPercentage(p.getDisabilityPercentage())
                .landOwnership(p.getLandOwnership())
                .landArea(p.getLandArea())
                .employmentStatus(p.getEmploymentStatus())
                .residenceType(p.getResidenceType())
                .maritalStatus(p.getMaritalStatus())
                .familySize(p.getFamilySize())
                .bplStatus(p.getBplStatus())
                .rationCardType(p.getRationCardType())
                .minorityStatus(p.getMinorityStatus())
                .exServiceman(p.getExServiceman())
                .completenessPercentage(p.getCompletenessPercentage())
                .build();
    }
}
