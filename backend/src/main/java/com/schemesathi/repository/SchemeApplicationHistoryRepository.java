package com.schemesathi.repository;

import com.schemesathi.entity.SchemeApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemeApplicationHistoryRepository extends JpaRepository<SchemeApplicationHistory, Long> {

    List<SchemeApplicationHistory> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    List<SchemeApplicationHistory> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
