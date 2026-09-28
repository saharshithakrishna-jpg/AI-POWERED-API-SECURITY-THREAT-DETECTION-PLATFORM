package com.apisecurity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "threat_events")
public class ThreatEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threat_id", nullable = false)
    private Threat threat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_request_id", nullable = false)
    private ApiRequest apiRequest;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
