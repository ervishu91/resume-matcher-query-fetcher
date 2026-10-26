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

    public ResumeMatchingService(
            EmbeddingService embeddingService,
            ResumeService resumeService,
            SimilarityService similarityService) {

        this.embeddingService = embeddingService;
        this.resumeService = resumeService;
        this.similarityService = similarityService;
    }

    public ResumeMatchResponse match(String jobDescription, int topK) {

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "Job description cannot be empty");
        }

        if (topK < 1) {
            throw new IllegalArgumentException(
                    "topK must be at least 1");
        }

        List<Double> jobEmbedding =
                embeddingService.generateEmbedding(jobDescription);

        List<ResumeMatchResult> matches =
                resumeService.getAllEmbeddedChunks()
                        .stream()
                        .map(chunk -> {
                            double score =
                                    similarityService.cosineSimilarity(
                                            jobEmbedding,
                                            chunk.getEmbedding());

                            return new ResumeMatchResult(
                                    chunk.getResumeId(),
                                    chunk.getChunkIndex(),
                                    chunk.getSection(),
                                    chunk.getText(),
                                    score);
                        })
                        .sorted(Comparator.comparingDouble(
                                ResumeMatchResult::getSimilarity).reversed())
                        .limit(topK)
                        .toList();

        // All results currently belong to the same or different resumes.
        // Retrieve the candidate profile for the highest-ranked result.
        CandidateProfile candidate = matches.isEmpty()
                ? null
                : resumeService.getCandidateProfile(
                matches.get(0).getResumeId()).orElse(null);

        return new ResumeMatchResponse(candidate, matches);
    }
}