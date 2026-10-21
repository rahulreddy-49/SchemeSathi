package com.schemesathi.repository;

import com.schemesathi.entity.HouseholdMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, Long> {

    List<HouseholdMember> findByUserId(Long userId);

    Optional<HouseholdMember> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndFullNameIgnoreCaseAndRelationshipIgnoreCase(Long userId, String fullName, String relationship);

    boolean existsByUserIdAndFullNameIgnoreCaseAndRelationshipIgnoreCaseAndIdNot(Long userId, String fullName, String relationship, Long id);

    void deleteByIdAndUserId(Long id, Long userId);
}
