package com.apisecurity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    // Simple controller to test the system internally, acting as an attack script
    @PostMapping("/traffic")
    public ResponseEntity<Map<String, String>> simulateTraffic(@RequestParam String type) {
        // Since we are running inside the server, simulating traffic directly against the controller
        // is tricky for the filter to catch if we just call methods. 
        // We'll instruct the frontend to loop calls for simulation instead of doing it from backend.
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "Simulation should be run from a client script or frontend.");
        
        return ResponseEntity.ok(response);
    }
}
