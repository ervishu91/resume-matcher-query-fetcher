package org.example.model;

public class ResumeMatchResult {

    private final String resumeId;
    private final int chunkIndex;
    private final String section;
    private final String text;
    private final double similarity;

    public ResumeMatchResult(
            String resumeId,
            int chunkIndex,
            String section,
            String text,
            double similarity) {

        this.resumeId = resumeId;
        this.chunkIndex = chunkIndex;
        this.section = section;
        this.text = text;
        this.similarity = similarity;
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

    public double getSimilarity() {
        return similarity;
    }
}