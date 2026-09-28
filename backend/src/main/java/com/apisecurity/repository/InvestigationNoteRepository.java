package com.apisecurity.repository;

import com.apisecurity.model.InvestigationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestigationNoteRepository extends JpaRepository<InvestigationNote, Long> {
    List<InvestigationNote> findByThreatIdOrderByCreatedAtDesc(Long threatId);
}
