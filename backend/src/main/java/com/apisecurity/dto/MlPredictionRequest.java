package com.apisecurity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MlPredictionRequest {
    private double requests_per_minute;
    private double failed_requests;
    private double unique_endpoints;
    private double avg_response_time;
    private double auth_failures;
}
