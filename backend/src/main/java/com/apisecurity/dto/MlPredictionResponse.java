package com.apisecurity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MlPredictionResponse {
    // Lombok @Data on a boolean field named "is_anomaly" generates broken getter isIs_anomaly().
    // Renamed to "anomaly" with @JsonProperty to correctly map JSON key "is_anomaly".
    @JsonProperty("is_anomaly")
    private boolean anomaly;

    @JsonProperty("anomaly_score")
    private double anomalyScore;
}
