package org.example.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import org.example.model.EmbeddedResumeChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import io.qdrant.client.grpc.JsonWithInt;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.grpc.Points.ReadConsistency;
import org.example.model.CandidateProfile;

@Service
public class QdrantVectorService {

    private static final String COLLECTION_NAME = "resume_chunks";

    private final QdrantClient qdrantClient;

    private static final String PROFILE_COLLECTION = "candidate_profiles";

    private final ObjectMapper objectMapper;

    public QdrantVectorService(QdrantClient qdrantClient, ObjectMapper objectMapper) {
        this.qdrantClient = qdrantClient;
        this.objectMapper = objectMapper;
    }

    public void saveChunk(EmbeddedResumeChunk chunk)
            throws ExecutionException, InterruptedException {

        List<Float> vector = chunk.getEmbedding()
                .stream()
                .map(Double::floatValue)
                .toList();

        Points.PointStruct point = Points.PointStruct.newBuilder()
                .setId(
                        Points.PointId.newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .build()
                )
                .setVectors(
                        Points.Vectors.newBuilder()
                                .setVector(
                                        Points.Vector.newBuilder()
                                                .addAllData(vector)
                                                .build()
                                )
                                .build()
                )
                .putPayload("resumeId", JsonWithInt.Value.newBuilder()
                        .setStringValue(chunk.getResumeId()).build())
                .putPayload("chunkIndex", JsonWithInt.Value.newBuilder()
                        .setIntegerValue(chunk.getChunkIndex()).build())
                .putPayload("section", JsonWithInt.Value.newBuilder()
                        .setStringValue(chunk.getSection() == null ? "" : chunk.getSection()).build())
                .putPayload("text", JsonWithInt.Value.newBuilder()
                        .setStringValue(chunk.getText()).build())
                .build();

        qdrantClient.upsertAsync(COLLECTION_NAME, List.of(point)).get();
    }
    public List<Points.ScoredPoint> searchSimilarChunks(
            List<Double> embedding,
            int topK
    ) throws ExecutionException, InterruptedException {

        List<Float> vector = embedding.stream()
                .map(Double::floatValue)
                .toList();

        Points.SearchPoints searchPoints = Points.SearchPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .setLimit(topK)
                .addAllVector(vector)
                .setWithPayload(
                        io.qdrant.client.grpc.Points.WithPayloadSelector.newBuilder()
                                .setEnable(true)
                                .build()
                )
                .build();

        List<Points.ScoredPoint> results =
                qdrantClient.searchAsync(searchPoints).get();

        System.out.println("Qdrant returned " + results.size() + " results");

        for (Points.ScoredPoint point : results) {
            System.out.println("Point ID: " + point.getId());
            System.out.println("Payload: " + point.getPayload());
        }

        return results;
    }

    public void saveCandidateProfile(CandidateProfile profile)
            throws Exception {

        // Use a deterministic UUID so the profile can be updated.
        UUID pointId = UUID.nameUUIDFromBytes(
                ("candidate-" + profile.getResumeId())
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        // Serialize the complete profile, including nested records and lists.
        String profileJson = objectMapper.writeValueAsString(profile);

        // Qdrant requires a vector. This collection is for profile storage,
        // not similarity searches, so use a placeholder vector.
        List<Float> placeholderVector =
                java.util.Collections.nCopies(768, 0.0f);

        Points.PointStruct point = Points.PointStruct.newBuilder()
                .setId(
                        Points.PointId.newBuilder()
                                .setUuid(pointId.toString())
                                .build()
                )
                .setVectors(
                        Points.Vectors.newBuilder()
                                .setVector(
                                        Points.Vector.newBuilder()
                                                .addAllData(placeholderVector)
                                                .build()
                                )
                                .build()
                )
                .putPayload(
                        "resumeId",
                        JsonWithInt.Value.newBuilder()
                                .setStringValue(profile.getResumeId())
                                .build()
                )
                .putPayload(
                        "profileJson",
                        JsonWithInt.Value.newBuilder()
                                .setStringValue(profileJson)
                                .build()
                )
                .build();

        qdrantClient.upsertAsync(
                PROFILE_COLLECTION,
                List.of(point)
        ).get();

        System.out.println(
                "Saved candidate profile to Qdrant: "
                        + profile.getResumeId()
        );
    }
    public java.util.Optional<CandidateProfile> getCandidateProfile(
            String resumeId) throws Exception {

        UUID pointId = UUID.nameUUIDFromBytes(
                ("candidate-" + resumeId)
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        Points.PointId qdrantPointId = Points.PointId.newBuilder()
                .setUuid(pointId.toString())
                .build();

        List<Points.RetrievedPoint> points = qdrantClient.retrieveAsync(
                PROFILE_COLLECTION,
                List.of(qdrantPointId),
                Points.WithPayloadSelector.newBuilder()
                        .setEnable(true)
                        .build(),
                Points.WithVectorsSelector.newBuilder()
                        .setEnable(false)
                        .build(),
                null
        ).get();

        if (points.isEmpty()) {
            System.out.println(
                    "No candidate profile found in Qdrant for resumeId: "
                            + resumeId
            );
            return java.util.Optional.empty();
        }

        Points.RetrievedPoint point = points.get(0);

        if (!point.getPayloadMap().containsKey("profileJson")) {
            System.out.println(
                    "Candidate profile payload is missing for resumeId: "
                            + resumeId
            );
            return java.util.Optional.empty();
        }

        String profileJson = point.getPayloadMap()
                .get("profileJson")
                .getStringValue();

        CandidateProfile profile =
                objectMapper.readValue(profileJson, CandidateProfile.class);

        System.out.println(
                "Retrieved candidate profile from Qdrant: " + resumeId
        );

        return java.util.Optional.of(profile);
    }
}