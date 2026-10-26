package org.example.service;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimilarityService {

    public double cosineSimilarity(
            List<Double> vectorA,
            List<Double> vectorB) {

        if (vectorA == null || vectorB == null
                || vectorA.isEmpty() || vectorB.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vectors cannot be null or empty");
        }

        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException(
                    "Embedding dimensions must match");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double a = vectorA.get(i);
            double b = vectorB.get(i);

            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0.0 || normB == 0.0) {
            throw new IllegalArgumentException(
                    "Cannot compare zero vectors");
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
