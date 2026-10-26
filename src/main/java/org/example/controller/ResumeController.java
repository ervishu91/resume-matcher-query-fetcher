package org.example.controller;

import org.example.model.CandidateProfile;
import org.example.model.EmbeddedResumeChunk;
import org.example.model.Resume;
import org.example.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collection;
import org.example.model.ResumeChunk;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public Resume uploadResume(@RequestParam("file") MultipartFile file)
            throws IOException {
        return resumeService.uploadResume(file);
    }

    @GetMapping("/{id}")
    public Resume getResume(@PathVariable String id) {
        return resumeService.getResume(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resume not found: " + id
                ));
    }

    @GetMapping
    public Collection<Resume> getAllResumes() {
        return resumeService.getAllResumes();
    }
    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<ResumeChunk>> getResumeChunks(
            @PathVariable String id) {

        if (resumeService.getResume(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                resumeService.getResumeChunks(id)
        );
    }
    @GetMapping("/{id}/profile")
    public ResponseEntity<CandidateProfile> getCandidateProfile(
            @PathVariable String id) {

        return resumeService.getCandidateProfile(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/embedded-chunks")
    public List<EmbeddedResumeChunk> getEmbeddedChunks(
            @PathVariable String id) {

        return resumeService.getEmbeddedChunks(id);
    }
}