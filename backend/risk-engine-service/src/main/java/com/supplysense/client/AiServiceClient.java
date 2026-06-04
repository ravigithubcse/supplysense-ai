package com.supplysense.client;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class AiServiceClient {

    private final WebClient webClient;

    public AiServiceClient(@Value("${ai-service.url:http://localhost:8090}") String aiServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    public Mono<RiskPredictionResponse> predictRisk(RiskPredictionRequest request) {
        return webClient.post()
                .uri("/api/v1/predict/risk")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RiskPredictionResponse.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.error("AI service risk prediction failed: {}", e.getMessage()))
                .onErrorReturn(buildFallbackPrediction(request.getSupplierId()));
    }

    public Mono<SentimentResponse> analyzeSentiment(String supplierId, String country) {
        return webClient.get()
                .uri(uri -> uri.path("/api/v1/sentiment")
                        .queryParam("supplierId", supplierId)
                        .queryParam("country", country)
                        .build())
                .retrieve()
                .bodyToMono(SentimentResponse.class)
                .timeout(Duration.ofSeconds(8))
                .doOnError(e -> log.warn("Sentiment analysis failed: {}", e.getMessage()))
                .onErrorReturn(new SentimentResponse(0.0, "NEUTRAL", List.of()));
    }

    public Mono<AnomalyResponse> detectAnomalies(UUID supplierId) {
        return webClient.get()
                .uri("/api/v1/anomaly/{supplierId}", supplierId)
                .retrieve()
                .bodyToMono(AnomalyResponse.class)
                .timeout(Duration.ofSeconds(8))
                .onErrorReturn(new AnomalyResponse(false, 0.0, List.of()));
    }

    private RiskPredictionResponse buildFallbackPrediction(String supplierId) {
        log.warn("Using fallback risk prediction for supplier: {}", supplierId);
        return RiskPredictionResponse.builder()
                .supplierId(supplierId)
                .compositeScore(50.0)
                .geopoliticalScore(50.0)
                .weatherScore(50.0)
                .financialScore(50.0)
                .logisticsScore(50.0)
                .sentimentScore(50.0)
                .forecastScore7d(50.0)
                .forecastScore30d(50.0)
                .confidence(0.3)
                .modelVersion("fallback-v1")
                .build();
    }

    // ---- DTOs ----

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskPredictionRequest {
        private String supplierId;
        private String country;
        private String industry;
        private Map<String, Object> historicalMetrics;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskPredictionResponse {
        private String supplierId;
        private Double compositeScore;
        private Double geopoliticalScore;
        private Double weatherScore;
        private Double financialScore;
        private Double logisticsScore;
        private Double sentimentScore;
        private Double forecastScore7d;
        private Double forecastScore30d;
        private Double confidence;
        private String modelVersion;
        private List<Map<String, Object>> factors;
        private List<String> mitigations;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SentimentResponse {
        private Double score;
        private String label;
        private List<String> topHeadlines;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AnomalyResponse {
        private Boolean anomalyDetected;
        private Double anomalyScore;
        private List<String> anomalies;
    }
}
