# AI-Powered Resume Matching System

A Java Spring Boot application that matches resumes against job descriptions using **text embeddings and cosine similarity**.

The application extracts information from PDF resumes, divides resume content into meaningful chunks, generates embeddings using a locally hosted embedding model, and ranks resume sections by their semantic similarity to a given job description.

The project is being developed incrementally, starting with semantic matching and moving toward persistent storage and LLM-powered candidate evaluation.

## Features

### Implemented

- **PDF Resume Parsing** — Extract text from uploaded resumes.
- **Text Normalization** — Normalize extracted text before processing.
- **Resume Chunking** — Divide resume content into sections such as summary, skills, work experience, and education.
- **Candidate Profile Extraction** — Extract candidate information, including name, email, phone number, experience, and skills.
- **Embedding Generation** — Generate vector embeddings using Ollama and `nomic-embed-text`.
- **Semantic Matching** — Compare job descriptions against resume chunks using cosine similarity.
- **Ranked Results** — Return the most relevant resume sections in descending order of similarity.
- **REST APIs** — Upload resumes, retrieve candidate profiles, inspect chunks, and match resumes against job descriptions.
- **In-Memory Storage** — Store resumes, profiles, chunks, and embeddings in application memory.

### Planned

- PostgreSQL persistence using Spring Data JPA.
- Persistent storage for resume chunks and embeddings.
- LLM-powered candidate evaluation and skill-gap analysis.
- Evidence-based explanations for candidate-job matches.

## Architecture

```text
                    Resume PDF
                        |
                        v
                   ResumeParser
                        |
                        v
                Text Normalization
                        |
                        v
                   ResumeChunker
                        |
              +---------+---------+
              |                   |
              v                   v
     Candidate Profile       EmbeddingService
        Extraction                 |
              |                    v
              |              Ollama API
              |           nomic-embed-text
              |                    |
              +---------+----------+
                        |
                        v
                 In-Memory Storage


                 Job Description
                        |
                        v
                 EmbeddingService
                        |
                        v
                 Job Description
                    Embedding
                        |
                        v
              Cosine Similarity
                        |
                        v
              Rank Resume Chunks
                        |
                        v
               Matching Response
```

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java |
| Backend Framework | Spring Boot |
| API Style | REST |
| Embedding Runtime | Ollama |
| Embedding Model | `nomic-embed-text` |
| Similarity Algorithm | Cosine similarity |
| Resume Input | PDF |
| Current Storage | In-memory Java collections |
| Planned Database | PostgreSQL |
| Planned Persistence Layer | Spring Data JPA |
| Planned LLM Integration | To be determined |

## Prerequisites

Before running the application, ensure you have:

- Java and a compatible JDK version for the project.
- Maven, or the Maven Wrapper included in the repository.
- Ollama installed and running locally.
- The `nomic-embed-text` embedding model downloaded.

Download Ollama from its official website:

https://ollama.com/

Pull the embedding model:

```bash
ollama pull nomic-embed-text
```

Start the Ollama service if it is not already running:

```bash
ollama serve
```

The application currently expects Ollama at:

```text
http://localhost:11434
```

## Getting Started

### 1. Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd <YOUR_PROJECT_DIRECTORY>
```

Replace the placeholders with your repository URL and project directory.

### 2. Verify Ollama

Check that Ollama is running and the embedding model is available:

```bash
ollama list
```

Confirm that `nomic-embed-text` appears in the output.

### 3. Run the Spring Boot application

Using Maven:

```bash
mvn spring-boot:run
```

Alternatively, if the repository includes the Maven Wrapper:

**macOS/Linux**

```bash
./mvnw spring-boot:run
```

**Windows**

```bash
mvnw.cmd spring-boot:run
```

Use the port configured in your Spring Boot application. If no custom port is configured, Spring Boot typically uses port `8080`.

## API Endpoints

The following endpoints are available in the current implementation.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/resumes` | Upload a PDF resume |
| `GET` | `/api/resumes` | Retrieve uploaded resumes |
| `GET` | `/api/resumes/{id}` | Retrieve a resume by ID |
| `GET` | `/api/resumes/{id}/chunks` | Retrieve chunks for a resume |
| `GET` | `/api/resumes/{id}/profile` | Retrieve the extracted candidate profile |
| `GET` | `/api/resumes/{id}/embedded-chunks` | Retrieve embedded resume chunks |
| `POST` | `/api/resumes/match` | Match resumes against a job description |

The exact request parameters and response structures depend on the controller implementation.

### Upload a Resume

Example using `curl`:

```bash
curl -X POST http://localhost:8080/api/resumes \
  -F "file=@/path/to/resume.pdf"
```

Replace `/path/to/resume.pdf` with the path to your PDF file.

### Match Resumes Against a Job Description

Example request:

```bash
curl -X POST http://localhost:8080/api/resumes/match \
  -H "Content-Type: application/json" \
  -d '{
    "jobDescription": "We are looking for a senior backend engineer with experience in Java, Spring Boot, distributed systems, Kafka, and cloud infrastructure."
  }'
```

This example assumes the matching endpoint accepts a JSON field named `jobDescription`. Adjust the payload if the current request DTO uses a different field name.

The response contains candidate details and ranked resume chunks, including their similarity scores.

Example response structure:

```json
{
  "candidate": {
    "resumeId": "example-resume-id",
    "name": "Candidate Name",
    "phone": "+91 9876543210",
    "email": "candidate@example.com",
    "totalExperienceYears": 9.0,
    "skills": [
      "Java",
      "Spring Boot",
      "Kafka",
      "Distributed Systems"
    ],
    "employmentHistory": [],
    "projects": []
  },
  "matches": [
    {
      "resumeId": "example-resume-id",
      "chunkIndex": 1,
      "section": "SUMMARY",
      "text": "Senior software engineer with experience building backend systems...",
      "similarity": 0.706
    }
  ]
}
```

The values above are illustrative. Actual results depend on the uploaded resume and job description. Employment history and projects are not yet populated by the current simplified profile extractor.

## How Matching Works

### 1. Resume ingestion

The application accepts a PDF resume and extracts its text.

### 2. Text processing

The extracted text is normalized and divided into chunks. Each chunk represents a portion of the resume, such as its summary, skills, work experience, or education.

### 3. Candidate profile extraction

The application extracts structured information such as the candidate's name, contact information, experience, and skills.

### 4. Embedding generation

Each resume chunk is passed to Ollama using the `nomic-embed-text` model to generate a numerical vector representation.

The job description is embedded using the same model.

### 5. Similarity calculation

The application computes cosine similarity between the job-description embedding and each resume-chunk embedding.

Cosine similarity measures the directional similarity between two vectors. It is not a calibrated probability or a percentage indicating the likelihood that a candidate is qualified.

### 6. Ranking

Resume chunks are sorted by similarity score in descending order, and the highest-ranked results are returned.

## Current Limitations

- **In-memory storage:** Resumes and embeddings are lost when the application restarts.
- **Heuristic profile extraction:** Extracted candidate information may be incomplete or inaccurate.
- **Employment and project extraction:** These fields are not currently populated by the simplified profile extractor.
- **Chunk-level ranking:** The current matching implementation ranks resume chunks rather than producing a comprehensive candidate-level score.
- **No LLM evaluation yet:** The application does not currently generate natural-language assessments or explain why a candidate matches a job.
- **No persistent database:** PostgreSQL integration is planned but not implemented.
- **Similarity score interpretation:** Scores should not be treated as match percentages or definitive hiring recommendations.

## Roadmap

### Phase 1: Semantic Resume Matching — Current

- [x] PDF text extraction
- [x] Resume text normalization
- [x] Resume chunking
- [x] Candidate profile extraction
- [x] Local embedding generation
- [x] Cosine similarity calculation
- [x] Ranked resume chunks
- [x] REST API for resume matching

### Phase 2: Database Persistence

- [ ] Integrate PostgreSQL
- [ ] Add JPA entities and repositories
- [ ] Persist resumes and candidate profiles
- [ ] Persist resume chunks and embeddings
- [ ] Load stored embeddings for matching
- [ ] Verify matching across application restarts

### Phase 3: LLM-Powered Evaluation

- [ ] Integrate an LLM
- [ ] Evaluate candidates against job requirements
- [ ] Identify matching and missing skills
- [ ] Generate evidence-based explanations
- [ ] Return structured evaluation results

### Phase 4: Improved Matching

- [ ] Aggregate chunk scores into candidate-level results
- [ ] Improve employment history and project extraction
- [ ] Evaluate matching quality using representative resumes and job descriptions
- [ ] Add automated tests for parsing, embeddings, and matching

## Development Philosophy

The project follows an incremental approach:

1. Build a working semantic matching pipeline.
2. Introduce persistent storage without disrupting matching.
3. Add LLM capabilities where they provide meaningful value.
4. Evaluate matching quality before adding unnecessary complexity.

The goal is to build a practical resume-matching system with a clear separation between document processing, embedding generation, retrieval, and candidate evaluation.

## License

Choose a license before publishing the repository publicly. No license has been specified for this project yet.