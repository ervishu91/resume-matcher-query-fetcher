package org.example.parser;

import org.example.model.ResumeChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResumeChunker {

    private static final int MAX_CHUNK_LENGTH = 1200;
    private static final int OVERLAP_LENGTH = 150;

    private static final Pattern SECTION_HEADING = Pattern.compile(
            "(?im)^\\s*(professional summary|summary|profile|"
                    + "technical skills|skills|work experience|"
                    + "professional experience|experience|employment history|"
                    + "projects|personal projects|education|certifications|"
                    + "achievements|responsibilities)\\s*:?\\s*$"
    );

    public List<ResumeChunk> chunk(String resumeId, String text) {
        List<ResumeChunk> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        Matcher matcher = SECTION_HEADING.matcher(text);
        List<Integer> headingPositions = new ArrayList<>();
        List<String> headings = new ArrayList<>();

        while (matcher.find()) {
            headingPositions.add(matcher.start());
            headings.add(matcher.group(1).trim());
        }

        if (headingPositions.isEmpty()) {
            addChunks(chunks, resumeId, "GENERAL", text);
            return chunks;
        }

        if (headingPositions.get(0) > 0) {
            String intro = text.substring(0, headingPositions.get(0)).trim();
            addChunks(chunks, resumeId, "INTRODUCTION", intro);
        }

        for (int i = 0; i < headingPositions.size(); i++) {
            int start = headingPositions.get(i);
            int end = (i + 1 < headingPositions.size())
                    ? headingPositions.get(i + 1)
                    : text.length();

            String sectionText = text.substring(start, end).trim();
            String section = headings.get(i).toUpperCase(Locale.ROOT);

            addChunks(chunks, resumeId, section, sectionText);
        }

        return chunks;
    }

    private void addChunks(
            List<ResumeChunk> chunks,
            String resumeId,
            String section,
            String text) {

        if (text == null || text.isBlank()) {
            return;
        }

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());

            // Prefer a nearby word boundary to avoid splitting words.
            if (end < text.length()) {
                int boundary = text.lastIndexOf(' ', end);

                if (boundary > start + MAX_CHUNK_LENGTH / 2) {
                    end = boundary;
                }
            }

            String chunkText = text.substring(start, end).trim();

            if (!chunkText.isBlank()) {
                chunks.add(new ResumeChunk(
                        resumeId,
                        chunks.size(),
                        section,
                        chunkText
                ));
            }

            if (end >= text.length()) {
                break;
            }

            // Overlap preserves context across adjacent chunks.
            start = Math.max(start + 1, end - OVERLAP_LENGTH);
        }
    }
}