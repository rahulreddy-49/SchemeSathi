package com.schemesathi.repository;

import com.schemesathi.entity.ApplicationStatus;
import com.schemesathi.entity.SchemeApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SchemeApplicationRepository extends JpaRepository<SchemeApplication, Long> {

    List<SchemeApplication> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<SchemeApplication> findByIdAndUserId(Long id, Long userId);

    List<SchemeApplication> findByUserIdAndSchemeId(Long userId, Long schemeId);

    List<SchemeApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    @Query("SELECT a FROM SchemeApplication a LEFT JOIN FETCH a.history WHERE a.id = :id AND a.user.id = :userId")
    Optional<SchemeApplication> findByIdAndUserIdWithHistory(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByUserIdAndSchemeIdAndHouseholdMemberIdAndStatusNotIn(
            Long userId, Long schemeId, Long householdMemberId, Collection<ApplicationStatus> statuses);

    boolean existsByUserIdAndSchemeIdAndHouseholdMemberIsNullAndStatusNotIn(
            Long userId, Long schemeId, Collection<ApplicationStatus> statuses);

    long countByUserIdAndStatus(Long userId, ApplicationStatus status);

    long countByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
