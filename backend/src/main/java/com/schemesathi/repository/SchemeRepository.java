package com.schemesathi.repository;

import com.schemesathi.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {

    List<Scheme> findByPublicationStatusAndActiveTrue(Scheme.PublicationStatus publicationStatus);

    default List<Scheme> findByActiveTrue() {
        return findByPublicationStatusAndActiveTrue(Scheme.PublicationStatus.PUBLISHED);
    }

    Optional<Scheme> findByIdAndActiveTrueAndPublicationStatus(Long id, Scheme.PublicationStatus publicationStatus);

    default Optional<Scheme> findPublishedById(Long id) {
        return findByIdAndActiveTrueAndPublicationStatus(id, Scheme.PublicationStatus.PUBLISHED);
    }

    @Query("SELECT s FROM Scheme s WHERE s.active = true AND s.publicationStatus = 'PUBLISHED' AND " +
           "(s.scope = 'CENTRAL' OR s.state = :state)")
    List<Scheme> findApplicableSchemes(@Param("state") String state);

    @Query("SELECT s FROM Scheme s WHERE s.active = true AND s.publicationStatus = 'PUBLISHED' AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(s.benefitCategory) LIKE LOWER(CONCAT('%',:query,'%')))")
    List<Scheme> searchSchemes(@Param("query") String query);

    List<Scheme> findByBenefitCategoryAndPublicationStatusAndActiveTrue(String benefitCategory, Scheme.PublicationStatus publicationStatus);

    default List<Scheme> findByBenefitCategoryAndActiveTrue(String benefitCategory) {
        return findByBenefitCategoryAndPublicationStatusAndActiveTrue(benefitCategory, Scheme.PublicationStatus.PUBLISHED);
    }

    List<Scheme> findByStateAndPublicationStatusAndActiveTrue(String state, Scheme.PublicationStatus publicationStatus);

    default List<Scheme> findByStateAndActiveTrue(String state) {
        return findByStateAndPublicationStatusAndActiveTrue(state, Scheme.PublicationStatus.PUBLISHED);
    }

    @Query("SELECT s FROM Scheme s WHERE s.active = true AND s.publicationStatus = 'PUBLISHED' AND s.benefitCategory = :category AND " +
           "(s.scope = 'CENTRAL' OR s.state = :state)")
    List<Scheme> findByCategoryAndState(@Param("category") String category, @Param("state") String state);

    // Admin methods:
    @Query("SELECT s FROM Scheme s WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:pubStatus IS NULL OR s.publicationStatus = :pubStatus) AND " +
           "(:verStatus IS NULL OR s.verificationStatus = :verStatus) AND " +
           "(:category IS NULL OR :category = '' OR s.benefitCategory = :category) AND " +
           "(:scope IS NULL OR s.scope = :scope) " +
           "ORDER BY s.updatedAt DESC")
    List<Scheme> findAllAdmin(
            @Param("query") String query,
            @Param("pubStatus") Scheme.PublicationStatus pubStatus,
            @Param("verStatus") Scheme.VerificationStatus verStatus,
            @Param("category") String category,
            @Param("scope") Scheme.SchemeScope scope);

    long countByPublicationStatus(Scheme.PublicationStatus status);

    long countByVerificationStatus(Scheme.VerificationStatus status);
}
