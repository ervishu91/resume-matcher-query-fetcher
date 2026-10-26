package org.example.model;

import java.time.Instant;

public class Resume {

    private final String id;
    private final String filename;
    private final String extractedText;
    private final Instant uploadedAt;

    public Resume(String id, String filename, String extractedText) {
        this.id = id;
        this.filename = filename;
        this.extractedText = extractedText;
        this.uploadedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}