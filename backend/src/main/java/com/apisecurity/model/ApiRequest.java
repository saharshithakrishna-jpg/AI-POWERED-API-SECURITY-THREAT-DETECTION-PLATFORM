package com.apisecurity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "api_requests")
public class ApiRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String sourceIp;

    private Long userId;

    @Column(nullable = false)
    private Integer responseStatus;

    @Column(nullable = false)
    private Integer responseTime;

    private String userAgent;

    private Integer requestSize;

    private Integer responseSize;

    private Boolean authenticated = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
