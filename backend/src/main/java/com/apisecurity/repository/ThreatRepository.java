package com.apisecurity.repository;

import com.apisecurity.model.Threat;
import com.apisecurity.model.ThreatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThreatRepository extends JpaRepository<Threat, Long> {
    List<Threat> findByStatus(ThreatStatus status);
    List<Threat> findByTimestampAfter(LocalDateTime timestamp);
    long countByStatus(ThreatStatus status);
}
