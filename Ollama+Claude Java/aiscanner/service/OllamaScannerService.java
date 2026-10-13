package com.example.aiscanner.service;

import com.example.aiscanner.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClient;

import java.util.Collections;

@Service
public class OllamaScannerService {
    private static final Logger log = LoggerFactory.getLogger(OllamaScannerService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OllamaScannerService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public ReviewScanResult scanReview(String reviewText) {
        String prompt = String.format(
            "Analyze this course review. Return ONLY a valid JSON object in this format: " +
            "{\"result\": \"pass\" | \"reject\", \"reasons\": [\"reason1\"]}. " +
            "Reject the review if it falls under: spam, profanity, purely emotional complaints, " +
            "or criticizing the instructor's personal style instead of the course content. " +
            "If it passes, the reasons array must be empty. \n\nReview text: %s",
            reviewText
        );

        OllamaRequest request = new OllamaRequest("qwen2.5:7b", prompt, false, "json");
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start();
            OllamaResponse ollamaResponse = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);
            stopWatch.stop();

            log.info("Ollama API latency: {} ms", stopWatch.getTotalTimeMillis());

            if (ollamaResponse == null || ollamaResponse.response() == null) {
                throw new RuntimeException("Empty response from Ollama API");
            }

            return objectMapper.readValue(ollamaResponse.response(), ReviewScanResult.class);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            return new ReviewScanResult("reject", Collections.singletonList("AI response format error"));
        } catch (Exception e) {
            log.error("Error during review scan: {}", e.getMessage());
            return new ReviewScanResult("reject", Collections.singletonList("Internal scanning error: " + e.getMessage()));
        }
    }
}
