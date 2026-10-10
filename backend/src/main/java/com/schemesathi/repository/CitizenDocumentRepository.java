package com.schemesathi.repository;

import com.schemesathi.entity.CitizenDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CitizenDocumentRepository extends JpaRepository<CitizenDocument, Long> {
    List<CitizenDocument> findByUserId(Long userId);
    List<CitizenDocument> findByUserIdAndDocumentType(Long userId, String documentType);
}
