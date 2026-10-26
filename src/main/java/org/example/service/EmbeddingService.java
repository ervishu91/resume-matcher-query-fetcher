package org.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestClient restClient;

    public EmbeddingService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public List<Double> generateEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "input", text
        );

        Map response = restClient.post()
                .uri("/api/embed")
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("embeddings")) {
            throw new IllegalStateException(
                    "Failed to generate embedding from Ollama"
            );
        }

        List<List<Double>> embeddings =
                (List<List<Double>>) response.get("embeddings");

        return embeddings.get(0);
    }
}