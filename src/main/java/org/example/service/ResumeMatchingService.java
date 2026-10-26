package org.example.service;

import org.example.model.ResumeMatchResult;
import org.example.model.ResumeMatchResponse;
import org.example.model.CandidateProfile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ResumeMatchingService {

    private final EmbeddingService embeddingService;
    private final ResumeService resumeService;
    private final SimilarityService similarityService;
    private final QdrantVectorService qdrantVectorService;

    public ResumeMatchingService(
            EmbeddingService embeddingService,
            ResumeService resumeService,
            SimilarityService similarityService, QdrantVectorService qdrantVectorService) {

        this.embeddingService = embeddingService;
        this.resumeService = resumeService;
        this.similarityService = similarityService;
        this.qdrantVectorService = qdrantVectorService;
    }

    public ResumeMatchResponse match(String jobDescription, int topK) throws Exception {

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "Job description cannot be empty");
        }

        if (topK < 1) {
            throw new IllegalArgumentException(
                    "topK must be at least 1");
        }

        // Step 1: Generate the job description embedding.
        List<Double> jobEmbedding =
                embeddingService.generateEmbedding(jobDescription);

        // Step 2: Search Qdrant for the most similar resume chunks.
        List<ResumeMatchResult> matches;

        try {
            matches = qdrantVectorService.searchSimilarChunks(jobEmbedding, topK)
                    .stream()
                    .map(point -> {
                        var payload = point.getPayload();

                        System.out.println("Qdrant point ID: " + point.getId());
                        System.out.println("Qdrant payload: " + payload);

                        String resumeId = payload.get("resumeId") != null
                                ? payload.get("resumeId").getStringValue()
                                : null;

                        System.out.println("Resume ID from Qdrant: " + resumeId);


                        int chunkIndex = payload.get("chunkIndex") != null
                                ? (int) payload.get("chunkIndex").getIntegerValue()
                                : -1;

                        String section = payload.get("section") != null
                                ? payload.get("section").getStringValue()
                                : "UNKNOWN";

                        String text = payload.get("text") != null
                                ? payload.get("text").getStringValue()
                                : "";

                        return new ResumeMatchResult(
                                resumeId, chunkIndex, section, text, point.getScore());
                    })
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Qdrant search was interrupted", e);

        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException(
                    "Qdrant search failed", e);
        }

        // Step 3: Retrieve the candidate profile for the highest-ranked result.
        CandidateProfile candidate = null;

        if (!matches.isEmpty()) {
            String resumeId = matches.get(0).getResumeId();

            if (resumeId != null) {
                candidate = qdrantVectorService
                        .getCandidateProfile(resumeId)
                        .orElse(null);
            }
        }
        // Step 4: Return the response.
        return new ResumeMatchResponse(candidate, matches);
    }
}