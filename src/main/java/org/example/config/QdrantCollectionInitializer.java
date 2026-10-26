package org.example.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class QdrantCollectionInitializer {

    private static final String RESUME_CHUNKS = "resume_chunks";
    private static final String CANDIDATE_PROFILES = "candidate_profiles";
    private static final long VECTOR_SIZE = 768;

    private final QdrantClient qdrantClient;

    public QdrantCollectionInitializer(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    @PostConstruct
    public void initializeCollections()
            throws ExecutionException, InterruptedException {

        createCollectionIfMissing(RESUME_CHUNKS);
        createCollectionIfMissing(CANDIDATE_PROFILES);
    }

    private void createCollectionIfMissing(String collectionName)
            throws ExecutionException, InterruptedException {

        boolean exists = qdrantClient
                .collectionExistsAsync(collectionName)
                .get();

        if (!exists) {
            qdrantClient.createCollectionAsync(
                    collectionName,
                    VectorParams.newBuilder()
                            .setSize(VECTOR_SIZE)
                            .setDistance(Distance.Cosine)
                            .build()
            ).get();

            System.out.println(
                    "Created Qdrant collection: " + collectionName
            );
        } else {
            System.out.println(
                    "Qdrant collection already exists: " + collectionName
            );
        }
    }
}