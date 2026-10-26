package org.example.controller;

import jakarta.annotation.PostConstruct;
import org.example.model.ResumeMatchResponse;
import org.example.model.ResumeMatchResult;
import org.example.model.ResumeMatchResult;
import org.example.service.ResumeMatchingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/resumes")
public class ResumeMatchingController {

    private final ResumeMatchingService resumeMatchingService;

    public ResumeMatchingController(
            ResumeMatchingService resumeMatchingService) {
        this.resumeMatchingService = resumeMatchingService;
    }

    @PostMapping("/match")
    public ResumeMatchResponse matchResume(
            @RequestBody Map<String, Object> request) {

        String jobDescription =
                (String) request.get("jobDescription");

        int topK = request.containsKey("topK")
                ? ((Number) request.get("topK")).intValue()
                : 5;

        return resumeMatchingService.match(jobDescription, topK);
    }
    @PostConstruct
    public void init() {
        System.out.println("ResumeMatchingController initialized");
    }
}