package com.schemesathi.service;

import com.schemesathi.dto.HouseholdMemberDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.HouseholdMember;
import com.schemesathi.entity.User;
import com.schemesathi.repository.HouseholdMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class HouseholdMemberService {

    private final HouseholdMemberRepository householdMemberRepository;

    public HouseholdMemberService(HouseholdMemberRepository householdMemberRepository) {
        this.householdMemberRepository = householdMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<HouseholdMemberDto> getHouseholdMembers(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return householdMemberRepository.findByUserId(user.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HouseholdMemberDto getHouseholdMember(User user, Long memberId) {
        HouseholdMember member = getMemberEntity(user, memberId);
        return toDto(member);
    }

    @Transactional
    public HouseholdMemberDto createHouseholdMember(User user, HouseholdMemberDto dto) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        String trimmedName = dto.getFullName() != null ? dto.getFullName().trim() : "";
        String trimmedRel = dto.getRelationship() != null ? dto.getRelationship().trim() : "";

        if (householdMemberRepository.existsByUserIdAndFullNameIgnoreCaseAndRelationshipIgnoreCase(
                user.getId(), trimmedName, trimmedRel)) {
            throw new IllegalArgumentException("A household member with the name '" + trimmedName + "' and relationship '" + trimmedRel + "' already exists in your household.");
        }

        HouseholdMember member = HouseholdMember.builder()
                .user(user)
                .fullName(trimmedName)
                .relationship(trimmedRel)
                .age(dto.getAge())
                .gender(dto.getGender())
                .state(dto.getState())
                .district(dto.getDistrict())
                .occupation(dto.getOccupation())
                .annualIncome(dto.getAnnualIncome())
                .educationLevel(dto.getEducationLevel())
                .category(dto.getCategory())
                .disabilityStatus(dto.getDisabilityStatus() != null ? dto.getDisabilityStatus() : false)
                .disabilityType(dto.getDisabilityType())
                .disabilityPercentage(dto.getDisabilityPercentage())
                .landOwnership(dto.getLandOwnership() != null ? dto.getLandOwnership() : false)
                .landArea(dto.getLandArea())
                .employmentStatus(dto.getEmploymentStatus())
                .residenceType(dto.getResidenceType())
                .maritalStatus(dto.getMaritalStatus())
                .familySize(dto.getFamilySize())
                .bplStatus(dto.getBplStatus() != null ? dto.getBplStatus() : false)
                .rationCardType(dto.getRationCardType())
                .minorityStatus(dto.getMinorityStatus() != null ? dto.getMinorityStatus() : false)
                .exServiceman(dto.getExServiceman() != null ? dto.getExServiceman() : false)
                .build();

        member = householdMemberRepository.save(member);
        return toDto(member);
    }

    @Transactional
    public HouseholdMemberDto updateHouseholdMember(User user, Long memberId, HouseholdMemberDto dto) {
        HouseholdMember member = getMemberEntity(user, memberId);

        String trimmedName = dto.getFullName() != null ? dto.getFullName().trim() : member.getFullName();
        String trimmedRel = dto.getRelationship() != null ? dto.getRelationship().trim() : member.getRelationship();

        if (householdMemberRepository.existsByUserIdAndFullNameIgnoreCaseAndRelationshipIgnoreCaseAndIdNot(
                user.getId(), trimmedName, trimmedRel, memberId)) {
            throw new IllegalArgumentException("Another household member with the name '" + trimmedName + "' and relationship '" + trimmedRel + "' already exists.");
        }

        member.setFullName(trimmedName);
        member.setRelationship(trimmedRel);
        member.setAge(dto.getAge());
        member.setGender(dto.getGender());
        member.setState(dto.getState());
        member.setDistrict(dto.getDistrict());
        member.setOccupation(dto.getOccupation());
        member.setAnnualIncome(dto.getAnnualIncome());
        member.setEducationLevel(dto.getEducationLevel());
        member.setCategory(dto.getCategory());
        member.setDisabilityStatus(dto.getDisabilityStatus() != null ? dto.getDisabilityStatus() : false);
        member.setDisabilityType(dto.getDisabilityType());
        member.setDisabilityPercentage(dto.getDisabilityPercentage());
        member.setLandOwnership(dto.getLandOwnership() != null ? dto.getLandOwnership() : false);
        member.setLandArea(dto.getLandArea());
        member.setEmploymentStatus(dto.getEmploymentStatus());
        member.setResidenceType(dto.getResidenceType());
        member.setMaritalStatus(dto.getMaritalStatus());
        member.setFamilySize(dto.getFamilySize());
        member.setBplStatus(dto.getBplStatus() != null ? dto.getBplStatus() : false);
        member.setRationCardType(dto.getRationCardType());
        member.setMinorityStatus(dto.getMinorityStatus() != null ? dto.getMinorityStatus() : false);
        member.setExServiceman(dto.getExServiceman() != null ? dto.getExServiceman() : false);

        member = householdMemberRepository.save(member);
        return toDto(member);
    }

    @Transactional
    public void deleteHouseholdMember(User user, Long memberId) {
        HouseholdMember member = getMemberEntity(user, memberId);
        householdMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public HouseholdMember getMemberEntity(User user, Long memberId) {
        if (user == null || memberId == null) {
            throw new IllegalArgumentException("User and memberId cannot be null");
        }
        return householdMemberRepository.findByIdAndUserId(memberId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Household member not found or access denied"));
    }

    public static CitizenProfile toTransientEntity(HouseholdMemberDto dto) {
        if (dto == null) {
            return null;
        }
        return CitizenProfile.builder()
                .age(dto.getAge())
                .gender(dto.getGender())
                .state(dto.getState())
                .district(dto.getDistrict())
                .occupation(dto.getOccupation())
                .annualIncome(dto.getAnnualIncome())
                .educationLevel(dto.getEducationLevel())
                .category(dto.getCategory())
                .disabilityStatus(dto.getDisabilityStatus() != null ? dto.getDisabilityStatus() : false)
                .disabilityType(dto.getDisabilityType())
                .disabilityPercentage(dto.getDisabilityPercentage())
                .landOwnership(dto.getLandOwnership() != null ? dto.getLandOwnership() : false)
                .landArea(dto.getLandArea())
                .employmentStatus(dto.getEmploymentStatus())
                .residenceType(dto.getResidenceType())
                .maritalStatus(dto.getMaritalStatus())
                .familySize(dto.getFamilySize())
                .bplStatus(dto.getBplStatus() != null ? dto.getBplStatus() : false)
                .rationCardType(dto.getRationCardType())
                .minorityStatus(dto.getMinorityStatus() != null ? dto.getMinorityStatus() : false)
                .exServiceman(dto.getExServiceman() != null ? dto.getExServiceman() : false)
                .build();
    }

    private HouseholdMemberDto toDto(HouseholdMember m) {
        return HouseholdMemberDto.builder()
                .id(m.getId())
                .fullName(m.getFullName())
                .relationship(m.getRelationship())
                .age(m.getAge())
                .gender(m.getGender())
                .state(m.getState())
                .district(m.getDistrict())
                .occupation(m.getOccupation())
                .annualIncome(m.getAnnualIncome())
                .educationLevel(m.getEducationLevel())
                .category(m.getCategory())
                .disabilityStatus(m.getDisabilityStatus())
                .disabilityType(m.getDisabilityType())
                .disabilityPercentage(m.getDisabilityPercentage())
                .landOwnership(m.getLandOwnership())
                .landArea(m.getLandArea())
                .employmentStatus(m.getEmploymentStatus())
                .residenceType(m.getResidenceType())
                .maritalStatus(m.getMaritalStatus())
                .familySize(m.getFamilySize())
                .bplStatus(m.getBplStatus())
                .rationCardType(m.getRationCardType())
                .minorityStatus(m.getMinorityStatus())
                .exServiceman(m.getExServiceman())
                .build();
    }
}
