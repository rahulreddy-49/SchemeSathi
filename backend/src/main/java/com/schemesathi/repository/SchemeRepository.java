package com.schemesathi.repository;

import com.schemesathi.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {

    List<Scheme> findByActiveTrue();

    @Query("SELECT s FROM Scheme s WHERE s.active = true AND " +
           "(s.scope = 'CENTRAL' OR s.state = :state)")
    List<Scheme> findApplicableSchemes(@Param("state") String state);

    @Query("SELECT s FROM Scheme s WHERE s.active = true AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(s.benefitCategory) LIKE LOWER(CONCAT('%',:query,'%')))")
    List<Scheme> searchSchemes(@Param("query") String query);

    List<Scheme> findByBenefitCategoryAndActiveTrue(String benefitCategory);

    List<Scheme> findByStateAndActiveTrue(String state);

    @Query("SELECT s FROM Scheme s WHERE s.active = true AND s.benefitCategory = :category AND " +
           "(s.scope = 'CENTRAL' OR s.state = :state)")
    List<Scheme> findByCategoryAndState(@Param("category") String category, @Param("state") String state);
}
