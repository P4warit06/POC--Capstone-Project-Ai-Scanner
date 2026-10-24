package com.example.aiscanner.service;

import com.example.aiscanner.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
public class GeminiScannerService {
    private static final Logger log = LoggerFactory.getLogger(GeminiScannerService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeminiScannerService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public ReviewScanResult scanReview(String reviewText) {
        String systemPrompt = "Analyze this Thai course review. Return ONLY a valid JSON object in this format: " +
                              "{\"result\": \"pass\" | \"reject\", \"reasons\": [\"reason1\"]}. " +
                              "Reject the review if it contains: spam, profanity, purely emotional complaints, " +
                              "or personal attacks on the instructor's teaching style instead of the course content. " +
                              "If it passes, the reasons array must be empty.";

        GeminiRequest.Part systemPart = new GeminiRequest.Part(systemPrompt);
        GeminiRequest.SystemInstruction systemInstruction = new GeminiRequest.SystemInstruction(List.of(systemPart));

        GeminiRequest.Part userPart = new GeminiRequest.Part(reviewText);
        GeminiRequest.Content userContent = new GeminiRequest.Content(List.of(userPart));

        GeminiRequest.GenerationConfig genConfig = new GeminiRequest.GenerationConfig("application/json");

        GeminiRequest request = new GeminiRequest(
            List.of(userContent),
            systemInstruction,
            genConfig
        );

        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start();
            GeminiResponse response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/models/gemini-1.5-flash:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);
            stopWatch.stop();

            log.info("Gemini API latency: {} ms", stopWatch.getTotalTimeMillis());

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new RuntimeException("Empty response from Gemini API");
            }

            String aiJsonResult = response.candidates().get(0)
                    .content().parts().get(0).text();

            return objectMapper.readValue(aiJsonResult, ReviewScanResult.class);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini AI response: {}", e.getMessage());
            return new ReviewScanResult("reject", Collections.singletonList("AI response format error"));
        } catch (Exception e) {
            log.error("Error during review scan: {}", e.getMessage());
            return new ReviewScanResult("reject", Collections.singletonList("Internal scanning error: " + e.getMessage()));
        }
    }
}
