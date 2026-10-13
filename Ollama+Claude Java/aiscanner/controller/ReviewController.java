package com.example.aiscanner.controller;

import com.example.aiscanner.dto.ReviewRequest;
import com.example.aiscanner.dto.ReviewScanResult;
import com.example.aiscanner.service.OllamaScannerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final OllamaScannerService scannerService;

    public ReviewController(OllamaScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping("/scan")
    public ReviewScanResult scan(@RequestBody ReviewRequest request) {
        return scannerService.scanReview(request.reviewText());
    }
}
