package com.apisecurity.service;

import com.apisecurity.dto.MlPredictionRequest;
import com.apisecurity.dto.MlPredictionResponse;
import com.apisecurity.model.*;
import com.apisecurity.repository.ApiRequestRepository;
import com.apisecurity.repository.ThreatEventRepository;
import com.apisecurity.repository.ThreatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreatDetectionService {

    private final ApiRequestRepository requestRepository;
    private final ThreatRepository threatRepository;
    private final ThreatEventRepository threatEventRepository;
    private final MlServiceClient mlServiceClient;

    // Rule thresholds
    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private static final int RATE_LIMIT_THRESHOLD = 100;
    private static final int TIME_WINDOW_MINUTES = 5;
    
    private static final List<String> SUSPICIOUS_ENDPOINTS = Arrays.asList(
            "/api/admin", "/api/internal", "/debug", "/config", "/actuator"
    );

    @Transactional
    public void analyzeRequest(ApiRequest request) {
        LocalDateTime timeWindow = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);
        String ip = request.getSourceIp();

        // 1. Rule-Based Detection
        boolean threatDetected = false;
        
        // A. Brute Force (Auth Failures)
        Long authFailures = requestRepository.countAuthFailuresByIpInWindow(ip, timeWindow);
        if (authFailures >= BRUTE_FORCE_THRESHOLD) {
            createOrUpdateThreat(ip, request, ThreatType.BRUTE_FORCE, 
                    "Multiple authentication failures detected from this IP.", 
                    DetectionMethod.RULE_BASED, authFailures.intValue(), null);
            threatDetected = true;
        }

        // B. Rate Limit Abuse
        Long totalRequests = requestRepository.countBySourceIpAndTimestampAfter(ip, timeWindow);
        if (totalRequests >= RATE_LIMIT_THRESHOLD) {
            createOrUpdateThreat(ip, request, ThreatType.RATE_LIMIT_ABUSE, 
                    "Unusually high volume of requests from this IP.", 
                    DetectionMethod.RULE_BASED, totalRequests.intValue(), null);
            threatDetected = true;
        }

        // C. Suspicious Endpoint Enumeration
        if (SUSPICIOUS_ENDPOINTS.stream().anyMatch(e -> request.getEndpoint().startsWith(e))) {
            // Only flag if it's unauthorized or they are hitting it repeatedly
            if (request.getResponseStatus() == 401 || request.getResponseStatus() == 403 || request.getResponseStatus() == 404) {
                createOrUpdateThreat(ip, request, ThreatType.ENDPOINT_ENUMERATION, 
                        "Access attempts to suspicious or internal endpoints.", 
                        DetectionMethod.RULE_BASED, 1, null);
                threatDetected = true;
            }
        }

        // 2. ML-Based Anomaly Detection (only if no obvious rule-based threat to avoid spam, or do both)
        if (!threatDetected) {
            checkMlAnomaly(ip, request, timeWindow);
        }
    }

    private void checkMlAnomaly(String ip, ApiRequest currentRequest, LocalDateTime timeWindow) {
        Long totalRequests = requestRepository.countBySourceIpAndTimestampAfter(ip, timeWindow);
        Long failedRequests = requestRepository.countFailedRequestsByIpInWindow(ip, timeWindow);
        Long uniqueEndpoints = requestRepository.countUniqueEndpointsByIpInWindow(ip, timeWindow);
        Long authFailures = requestRepository.countAuthFailuresByIpInWindow(ip, timeWindow);
        Double avgResponseTime = requestRepository.getAverageResponseTimeByIpInWindow(ip, timeWindow);

        MlPredictionRequest mlReq = MlPredictionRequest.builder()
                .requests_per_minute(totalRequests != null ? totalRequests / (double)TIME_WINDOW_MINUTES : 1)
                .failed_requests(failedRequests != null ? failedRequests : 0)
                .unique_endpoints(uniqueEndpoints != null ? uniqueEndpoints : 1)
                .avg_response_time(avgResponseTime != null ? avgResponseTime : currentRequest.getResponseTime())
                .auth_failures(authFailures != null ? authFailures : 0)
                .build();

        MlPredictionResponse mlRes = mlServiceClient.predictAnomaly(mlReq);

        // Fixed: is_anomaly() → isAnomaly(), getAnomaly_score() → getAnomalyScore()
        if (mlRes.isAnomaly()) {
            createOrUpdateThreat(ip, currentRequest, ThreatType.ANOMALOUS_BEHAVIOR,
                    "Machine learning model detected anomalous behavior pattern.",
                    DetectionMethod.ML_BASED, totalRequests.intValue(), mlRes.getAnomalyScore());
        }
    }

    private void createOrUpdateThreat(String ip, ApiRequest request, ThreatType type, String desc, 
                                      DetectionMethod method, int reqCount, Double mlScore) {
        
        Threat threat = new Threat();
        threat.setThreatType(type);
        threat.setDetectionMethod(method);
        threat.setSourceIp(ip);
        threat.setEndpoint(request.getEndpoint());
        threat.setHttpMethod(request.getHttpMethod());
        threat.setTimestamp(request.getTimestamp());
        threat.setRequestCount(reqCount);
        threat.setAuthenticationFailures(request.getResponseStatus() == 401 ? 1 : 0);
        threat.setMlAnomalyScore(mlScore);
        threat.setDescription(desc);
        threat.setRecommendedAction("Investigate IP traffic and block if malicious.");
        
        // Calculate Risk Score
        int score = calculateRiskScore(type, reqCount, mlScore);
        threat.setRiskScore(score);
        threat.setSeverity(calculateSeverity(score));
        
        threat = threatRepository.save(threat);

        ThreatEvent event = new ThreatEvent();
        event.setThreat(threat);
        event.setApiRequest(request);
        threatEventRepository.save(event);
        
        log.warn("Threat detected: {} from IP {}", type, ip);
    }

    private int calculateRiskScore(ThreatType type, int count, Double mlScore) {
        int baseScore = 0;
        switch (type) {
            case BRUTE_FORCE: baseScore = 70; break;
            case RATE_LIMIT_ABUSE: baseScore = 40; break;
            case ENDPOINT_ENUMERATION: baseScore = 60; break;
            case UNAUTHORIZED_ACCESS: baseScore = 50; break;
            case ANOMALOUS_BEHAVIOR: baseScore = 60; break;
            default: baseScore = 30;
        }

        // Add frequency contribution
        int freqContribution = Math.min(count / 2, 20); // max 20 points for frequency
        
        // Add ML contribution if present
        int mlContribution = 0;
        if (mlScore != null) {
            mlContribution = (int) (mlScore * 20); // max 20 points for high ML score
        }

        int finalScore = baseScore + freqContribution + mlContribution;
        return Math.min(finalScore, 100); // Max 100
    }

    private Severity calculateSeverity(int score) {
        if (score <= 30) return Severity.LOW;
        if (score <= 60) return Severity.MEDIUM;
        if (score <= 80) return Severity.HIGH;
        return Severity.CRITICAL;
    }
}
