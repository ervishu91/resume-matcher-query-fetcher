package org.example.model;

public class ResumeChunk {

    private final String resumeId;
    private final int chunkIndex;
    private final String section;
    private final String text;

    public ResumeChunk(
            String resumeId,
            int chunkIndex,
            String section,
            String text) {
        this.resumeId = resumeId;
        this.chunkIndex = chunkIndex;
        this.section = section;
        this.text = text;
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
}