package org.example.service;

import org.example.model.CandidateProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CandidateProfileExtractor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\+?\\d[\\d\\s().-]{7,}\\d)(?!\\d)"
    );

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(?i)(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*years?\\s+of\\s+experience"
    );

    private static final List<String> KNOWN_SKILLS = List.of(
            "Java",
            "Python",
            "Spring Boot",
            "Kafka",
            "Redis",
            "Hibernate",
            "AWS",
            "OCI",
            "DynamoDB",
            "PostgreSQL",
            "MySQL",
            "Microservices",
            "Distributed Systems",
            "REST APIs",
            "SQS",
            "Step Functions",
            "S3",
            "EC2",
            "CloudWatch"
    );

    public CandidateProfile extract(String resumeId, String text) {

        if (text == null || text.isBlank()) {
            return new CandidateProfile(
                    resumeId,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        String name = extractName(text);
        String phone = extractPhone(text);
        String email = extractEmail(text);
        Double experience = extractExperience(text);
        List<String> skills = extractSkills(text);

        return new CandidateProfile(
                resumeId,
                name,
                phone,
                email,
                experience,
                skills,
                List.of(),
                List.of()
        );
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);

        return matcher.find()
                ? matcher.group()
                : null;
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);

        while (matcher.find()) {
            String candidate = matcher.group().trim();

            long digitCount = candidate.chars()
                    .filter(Character::isDigit)
                    .count();

            if (digitCount >= 10 && digitCount <= 15) {
                String digits = candidate.replaceAll("\\D", "");

                if (digits.length() == 12 && digits.startsWith("91")) {
                    return "+91 " + digits.substring(2);
                }

                return candidate;
            }
        }

        return null;
    }

    private String extractName(String text) {
        String[] lines = text.split("\\R");

        for (String line : lines) {
            String candidate = line.trim();

            if (candidate.isEmpty()) {
                continue;
            }

            // Skip common headings and contact-detail lines.
            if (candidate.matches("(?i)resume|curriculum vitae|cv")) {
                continue;
            }

            if (EMAIL_PATTERN.matcher(candidate).find()) {
                continue;
            }

            if (PHONE_PATTERN.matcher(candidate).find()) {
                continue;
            }

            if (candidate.matches("(?i).*(software engineer|developer|manager|architect).*")) {
                continue;
            }

            // Conservative heuristic: accept a short, name-like line.
            if (candidate.matches("[\\p{L}][\\p{L} .'-]{1,59}")) {
                return candidate;
            }
        }

        return null;
    }

    private Double extractExperience(String text) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(text);

        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private List<String> extractSkills(String text) {
        String lowerText = text.toLowerCase();
        List<String> skills = new ArrayList<>();

        for (String skill : KNOWN_SKILLS) {
            if (lowerText.contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }

        return skills;
    }


}