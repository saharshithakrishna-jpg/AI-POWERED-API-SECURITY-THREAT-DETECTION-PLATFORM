package com.apisecurity.repository;

import com.apisecurity.model.ThreatEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreatEventRepository extends JpaRepository<ThreatEvent, Long> {
    List<ThreatEvent> findByThreatId(Long threatId);
}
