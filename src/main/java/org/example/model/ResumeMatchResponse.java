package org.example.model;

import java.util.List;

public class ResumeMatchResponse {

    private final CandidateProfile candidate;
    private final List<ResumeMatchResult> matches;

    public ResumeMatchResponse(
            CandidateProfile candidate,
            List<ResumeMatchResult> matches) {
        this.candidate = candidate;
        this.matches = List.copyOf(matches);
    }

    public CandidateProfile getCandidate() {
        return candidate;
    }

    public List<ResumeMatchResult> getMatches() {
        return matches;
    }
}
