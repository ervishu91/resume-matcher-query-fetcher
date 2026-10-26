package org.example.service;

import org.apache.tika.exception.TikaException;
import org.example.model.EmbeddedResumeChunk;
import org.example.model.Resume;
import org.example.model.ResumeChunk;
import org.example.parser.ResumeChunker;
import org.example.parser.ResumeParser;
import org.example.parser.ResumeTextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.example.model.CandidateProfile;

@Service
public class ResumeService {

    private final ResumeParser resumeParser;
    private final ResumeTextNormalizer textNormalizer;
    private final ResumeChunker resumeChunker;

    // Temporary in-memory storage for resumes.
    private final Map<String, Resume> resumes = new ConcurrentHashMap<>();

    // Temporary in-memory storage for resume chunks.
    private final Map<String, List<ResumeChunk>> resumeChunks =
            new ConcurrentHashMap<>();

    private final CandidateProfileExtractor candidateProfileExtractor;

    private final EmbeddingService  embeddingService;

    private final Map<String, CandidateProfile> candidateProfiles =
            new ConcurrentHashMap<>();

    private final Map<String, List<EmbeddedResumeChunk>> embeddedChunks =
            new ConcurrentHashMap<>();

    public ResumeService(
            ResumeParser resumeParser,
            ResumeTextNormalizer textNormalizer,
            ResumeChunker resumeChunker,
            CandidateProfileExtractor candidateProfileExtractor, EmbeddingService embeddingService) {

        this.resumeParser = resumeParser;
        this.textNormalizer = textNormalizer;
        this.resumeChunker = resumeChunker;
        this.candidateProfileExtractor = candidateProfileExtractor;
        this.embeddingService = embeddingService;
    }

    public Resume uploadResume(MultipartFile file) throws IOException {
        String extractedText;

        try {
            extractedText = resumeParser.extractText(file);
        } catch (TikaException e) {
            throw new IOException("Failed to extract text from resume", e);
        }

        // Step 1: Clean the extracted text.
        String normalizedText = textNormalizer.normalize(extractedText);

        // Step 2: Generate a single ID for the resume and its chunks.
        String id = UUID.randomUUID().toString();

        // Step 3: Split the normalized text into chunks.
        List<ResumeChunk> chunks =
                resumeChunker.chunk(id, normalizedText);

        // Step 4: Create and store the resume.
        Resume resume = new Resume(
                id,
                file.getOriginalFilename(),
                normalizedText
        );

        resumes.put(id, resume);
        resumeChunks.put(id, chunks);

        CandidateProfile profile =
                candidateProfileExtractor.extract(id, normalizedText);

        candidateProfiles.put(id, profile);
        List<EmbeddedResumeChunk> embedded = chunks.stream()
                .map(chunk -> new EmbeddedResumeChunk(
                        resume.getId(),
                        chunk.getChunkIndex(),
                        chunk.getSection(),
                        chunk.getText(),
                        embeddingService.generateEmbedding(chunk.getText())
                ))
                .toList();

        embeddedChunks.put(resume.getId(), embedded);

        return resume;
    }

    public Optional<Resume> getResume(String id) {
        return Optional.ofNullable(resumes.get(id));
    }

    public Collection<Resume> getAllResumes() {
        return resumes.values();
    }

    public List<ResumeChunk> getResumeChunks(String resumeId) {
        return resumeChunks.getOrDefault(resumeId, List.of());
    }
    public Optional<CandidateProfile> getCandidateProfile(String resumeId) {
        return Optional.ofNullable(candidateProfiles.get(resumeId));
    }
    public List<EmbeddedResumeChunk> getEmbeddedChunks(String resumeId) {
        return embeddedChunks.getOrDefault(resumeId, List.of());
    }
    public List<EmbeddedResumeChunk> getAllEmbeddedChunks() {
        return embeddedChunks.values()
                .stream()
                .flatMap(List::stream)
                .toList();
    }
}