package com.apisecurity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InternalController {

    // ── Real health endpoint ──────────────────────────────────────────────
    @GetMapping("/api/internal/health")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("System is healthy.");
    }

    // ── Honeypot / enumeration-target endpoints ───────────────────────────
    // These exist so the monitoring filter records them; they return 403
    // so the threat engine's responseStatus check (401/403/404) is satisfied.

    @GetMapping("/api/admin")
    public ResponseEntity<Map<String, String>> adminForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }

    @GetMapping("/api/internal/users")
    public ResponseEntity<Map<String, String>> internalUsersForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }

    @GetMapping("/api/internal/debug")
    public ResponseEntity<Map<String, String>> internalDebugForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }

    @GetMapping("/api/config")
    public ResponseEntity<Map<String, String>> configForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }

    @GetMapping("/api/actuator/env")
    public ResponseEntity<Map<String, String>> actuatorEnvForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }
}
