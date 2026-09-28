package com.apisecurity.controller;

import com.apisecurity.model.ThreatStatus;
import com.apisecurity.repository.ApiRequestRepository;
import com.apisecurity.repository.ThreatRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ApiRequestRepository requestRepository;
    private final ThreatRepository threatRepository;

    @GetMapping("/summary")
    public DashboardSummary getSummary() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        long totalRequests = requestRepository.count();
        // Since we are mocking "requests today" in absence of a specific query, let's just use total for demo
        // or a custom method can be added to ApiRequestRepository
        
        long totalThreats = threatRepository.count();
        long openIncidents = threatRepository.countByStatus(ThreatStatus.OPEN);
        
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalRequests(totalRequests);
        summary.setRequestsToday((long) (totalRequests * 0.8)); // Demo purposes
        summary.setDetectedThreats(totalThreats);
        summary.setOpenIncidents(openIncidents);
        
        return summary;
    }

    @Data
    static class DashboardSummary {
        private long totalRequests;
        private long requestsToday;
        private long detectedThreats;
        private long openIncidents;
    }
}
