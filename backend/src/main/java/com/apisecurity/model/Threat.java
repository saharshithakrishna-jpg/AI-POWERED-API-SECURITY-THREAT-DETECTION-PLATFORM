package com.apisecurity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "threats")
public class Threat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatType threatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionMethod detectionMethod;

    @Column(nullable = false)
    private String sourceIp;

    private String endpoint;
    private String httpMethod;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Integer requestCount;
    private Integer authenticationFailures;
    private Double mlAnomalyScore;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Enumerated(EnumType.STRING)
    private ThreatStatus status = ThreatStatus.OPEN;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
