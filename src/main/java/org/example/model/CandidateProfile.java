package org.example.model;

import java.util.List;

public class CandidateProfile {

    private final String resumeId;
    private final String name;
    private final String phone;
    private final String email;
    private final Double totalExperienceYears;
    private final List<String> skills;
    private final List<Employment> employmentHistory;
    private final List<Project> projects;

    public CandidateProfile(
            String resumeId,
            String name,
            String phone,
            String email,
            Double totalExperienceYears,
            List<String> skills,
            List<Employment> employmentHistory,
            List<Project> projects) {

        this.resumeId = resumeId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.totalExperienceYears = totalExperienceYears;
        this.skills = List.copyOf(skills);
        this.employmentHistory = List.copyOf(employmentHistory);
        this.projects = List.copyOf(projects);
    }

    public String getResumeId() {
        return resumeId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Double getTotalExperienceYears() {
        return totalExperienceYears;
    }

    public List<String> getSkills() {
        return skills;
    }

    public List<Employment> getEmploymentHistory() {
        return employmentHistory;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public record Employment(
            String company,
            String role,
            String startDate,
            String endDate) {
    }

    public record Project(
            String name,
            String description,
            List<String> technologies) {
    }
}