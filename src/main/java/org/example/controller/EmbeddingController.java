package org.example.controller;

import org.example.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/test")
    public Map<String, Object> testEmbedding(
            @RequestBody Map<String, String> request) {

        String text = request.get("text");

        var embedding = embeddingService.generateEmbedding(text);

        return Map.of(
                "dimensions", embedding.size(),
                "embedding", embedding
        );
    }
}