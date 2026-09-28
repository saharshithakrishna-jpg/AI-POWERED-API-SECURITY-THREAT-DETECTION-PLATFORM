package com.apisecurity.service;

import com.apisecurity.dto.MlPredictionRequest;
import com.apisecurity.dto.MlPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MlServiceClient {

    private final WebClient webClient;

    public MlServiceClient(@Value("${app.ml.service.url}") String mlServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    public MlPredictionResponse predictAnomaly(MlPredictionRequest request) {
        try {
            return webClient.post()
                    .body(Mono.just(request), MlPredictionRequest.class)
                    .retrieve()
                    .bodyToMono(MlPredictionResponse.class)
                    .block(); // Blocking since we want to act on it immediately in this architecture
        } catch (Exception e) {
            // Fallback if ML service is down - return safe defaults
            MlPredictionResponse fallback = new MlPredictionResponse();
            fallback.setAnomaly(false);        // Fixed: was set_anomaly() which doesn't compile
            fallback.setAnomalyScore(0.0);     // Fixed: was setAnomaly_score() which doesn't compile
            return fallback;
        }
    }
}

