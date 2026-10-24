package com.example.aiscanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiRequest(
    List<Content> contents,
    @JsonProperty("system_instruction") SystemInstruction systemInstruction,
    @JsonProperty("generation_config") GenerationConfig generationConfig
) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
    public record SystemInstruction(List<Part> parts) {}
    public record GenerationConfig(
        @JsonProperty("response_mime_type") String responseMimeType
    ) {}
}
