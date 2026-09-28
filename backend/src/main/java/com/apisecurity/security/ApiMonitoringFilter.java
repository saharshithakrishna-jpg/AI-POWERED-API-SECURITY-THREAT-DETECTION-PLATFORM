package com.apisecurity.security;

import com.apisecurity.model.ApiRequest;
import com.apisecurity.repository.ApiRequestRepository;
import com.apisecurity.service.ThreatDetectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiMonitoringFilter extends OncePerRequestFilter {

    private final ApiRequestRepository requestRepository;
    private final ThreatDetectionService threatDetectionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Only monitor API endpoints, avoid static assets
            if (request.getRequestURI().startsWith("/api/")) {
                saveRequestAndAnalyze(request, response, requestId, duration);
            }
        }
    }

    private void saveRequestAndAnalyze(HttpServletRequest request, HttpServletResponse response, String requestId, long duration) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");

        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setRequestId(requestId);
        apiRequest.setTimestamp(LocalDateTime.now());
        apiRequest.setHttpMethod(request.getMethod());
        apiRequest.setEndpoint(request.getRequestURI());
        
        // Handle proxies (simplistic for demo)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        apiRequest.setSourceIp(ip);
        
        // We could extract User ID if auth principal is our custom UserDetails
        // apiRequest.setUserId(...)
        
        apiRequest.setResponseStatus(response.getStatus());
        apiRequest.setResponseTime((int) duration);
        apiRequest.setUserAgent(request.getHeader("User-Agent"));
        apiRequest.setAuthenticated(isAuthenticated);
        
        // Save to DB
        apiRequest = requestRepository.save(apiRequest);
        
        // Analyze asynchronously or synchronously (synchronous for simplicity in this project)
        threatDetectionService.analyzeRequest(apiRequest);
    }
}
