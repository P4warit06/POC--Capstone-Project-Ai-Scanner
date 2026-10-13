package com.example.aiscanner.dto;

public record OllamaRequest(
    String model,
    String prompt,
    boolean stream,
    String format
) {}
