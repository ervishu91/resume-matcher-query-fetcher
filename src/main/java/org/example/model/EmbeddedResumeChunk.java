package org.example.model;

import java.util.List;

public class EmbeddedResumeChunk {

    private final String resumeId;
    private final int chunkIndex;
    private final String section;
    private final String text;
    private final List<Double> embedding;

    public EmbeddedResumeChunk(
            String resumeId,
            int chunkIndex,
            String section,
            String text,
            List<Double> embedding) {

        this.resumeId = resumeId;
        this.chunkIndex = chunkIndex;
        this.section = section;
        this.text = text;
        this.embedding = List.copyOf(embedding);
    }

    public String getResumeId() {
        return resumeId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getSection() {
        return section;
    }

    public String getText() {
        return text;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }
}