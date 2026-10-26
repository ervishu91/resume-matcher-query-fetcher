# AI-Powered Resume Matching System

A Java Spring Boot application that matches resumes against job descriptions using **text embeddings and Qdrant vector search with cosine similarity**.

The application extracts information from PDF resumes, divides resume content into meaningful chunks, generates embeddings using a locally hosted embedding model, and ranks resume sections by their semantic similarity to a given job description.

The project is being developed incrementally. Semantic retrieval now uses Qdrant for persistent vector storage, while further cleanup is planned to remove redundant in-memory state, handle duplicate uploads, and improve candidate-level ranking.

## Features

### Implemented

- **PDF Resume Parsing** — Extract text from uploaded resumes.
- **Text Normalization** — Normalize extracted text before processing.
- **Resume Chunking** — Divide resume content into sections such as summary, skills, work experience, and education.
- **Candidate Profile Extraction** — Extract candidate information, including name, email, phone number, experience, and skills.
- **Embedding Generation** — Generate 768-dimensional vector embeddings using Ollama and `nomic-embed-text`.
- **Qdrant Vector Database** — Run Qdrant locally and create `resume_chunks` and `candidate_profiles` collections using cosine distance.
- **Persistent Chunk Storage** — Store resume-chunk vectors and payload fields (`resumeId`, `chunkIndex`, `section`, and `text`) in Qdrant.
- **Vector Search** — Search Qdrant using the job-description embedding and return ranked chunks with similarity scores.
- **Persistent Candidate Profiles** — Store serialized candidate profiles in Qdrant and retrieve them by deterministic point ID, including after a Spring Boot restart.
- **REST APIs** — Upload resumes, retrieve candidate profiles, inspect chunks, and match resumes against job descriptions.

### Planned

- Remove redundant in-memory storage for data that is already persisted in Qdrant.
- Deduplicate matching results at the candidate level and decide how to handle repeated uploads of the same resume.
- Add a configurable similarity threshold and improve candidate-level ranking.
- PostgreSQL persistence for resume metadata and other structured application data, if needed.
- LLM-powered candidate evaluation, skill-gap analysis, and evidence-based explanations.

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
| Similarity Algorithm | Qdrant cosine vector search |
| Resume Input | PDF |
| Vector Database | Qdrant |
| Vector Database Client | `io.qdrant:client:1.15.0` |
| Embedding Dimensions | 768 |
| Embedding Storage | Qdrant `resume_chunks` collection |
| Candidate Profile Storage | Qdrant `candidate_profiles` collection |
| Additional Structured Database | PostgreSQL, optional/planned |
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

## Qdrant Setup

The application connects to a locally running Qdrant instance using its gRPC port `6334`. The Qdrant dashboard/API is available on port `6333`.

The current configuration expects Qdrant at `localhost:6334`. Ensure the Qdrant container is running before starting the Spring Boot application. The application initializes these collections if they do not exist:

- `resume_chunks` — 768-dimensional vectors using cosine distance, with resume ID, chunk index, section, and chunk text in the payload.
- `candidate_profiles` — 768-dimensional vectors using cosine distance, with the resume ID and serialized candidate profile in the payload. The profile is retrieved by a deterministic point ID.

Example local Docker command, if you need to start Qdrant separately:

```bash
docker run -d --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v qdrant_storage:/qdrant/storage \
  qdrant/qdrant
```

If a container named `qdrant` already exists, start that existing container instead of creating another one.

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

### 5. Persistent vector storage

The application stores each resume-chunk embedding and its payload in Qdrant's `resume_chunks` collection. Candidate profiles are serialized and stored in the `candidate_profiles` collection.

### 6. Vector search and ranking

The job-description embedding is sent to Qdrant, which returns the nearest resume chunks using cosine distance. Results include similarity scores and the chunk payload.

Cosine similarity is not a calibrated probability or a percentage indicating the likelihood that a candidate is qualified. The current endpoint ranks chunks, not unique candidates.

## Current Limitations

- **Partial in-memory state remains:** Resume objects, resume chunks, candidate profiles, and embedded chunks are still held in Java maps for some existing endpoints and workflows. Qdrant now persists vectors/chunk payloads and candidate profiles, but the application has not yet been fully refactored to treat Qdrant as the source of truth for all relevant reads.
- **Duplicate uploads:** Uploading the same resume more than once creates different resume IDs and can produce duplicate-looking search results.
- **Chunk-level ranking:** The matching implementation ranks chunks. It does not yet aggregate results into one ranked result per candidate.
- **No configurable similarity threshold:** The endpoint can return weak matches unless a threshold is added.
- **Heuristic profile extraction:** Extracted candidate information may be incomplete or inaccurate.
- **Employment and project extraction:** These fields are not currently populated by the simplified profile extractor.
- **No LLM evaluation yet:** The application does not currently generate natural-language assessments or explain why a candidate matches a job.
- **PostgreSQL not integrated:** Qdrant handles vector retrieval and profile payload storage; PostgreSQL remains optional/planned for structured metadata and relational queries.
- **Similarity score interpretation:** Scores should not be treated as match percentages or definitive hiring recommendations.

## Roadmap

### Phase 1: Semantic Resume Matching — Completed

- [x] PDF text extraction
- [x] Resume text normalization
- [x] Resume chunking
- [x] Candidate profile extraction
- [x] Local embedding generation
- [x] Qdrant vector search using cosine distance
- [x] Ranked resume chunks
- [x] REST API for resume matching

### Phase 2: Qdrant Vector Persistence — Implemented

- [x] Run Qdrant locally with persistent Docker volume
- [x] Create `resume_chunks` collection (768 dimensions, cosine distance)
- [x] Create `candidate_profiles` collection
- [x] Persist resume-chunk vectors and payloads in Qdrant
- [x] Persist serialized candidate profiles in Qdrant
- [x] Query Qdrant for matching chunks instead of calculating similarity over the in-memory embedding list
- [x] Retrieve candidate profiles from Qdrant by deterministic point ID
- [x] Verify matching/profile retrieval after restarting Spring Boot

**Note:** This phase implements Qdrant persistence for vectors/chunks and candidate profiles. It does not mean every resume-related object or endpoint has been migrated off in-memory maps. PostgreSQL/JPA has not been implemented.

### Phase 3: Remove Redundant In-Memory State and Improve Results — Next

- [ ] Review `ResumeService` and identify which in-memory maps are still required by existing endpoints.
- [ ] Stop using `embeddedChunks` as the source for matching; use Qdrant for vector retrieval.
- [ ] Migrate profile reads used by matching to `QdrantVectorService.getCandidateProfile()`.
- [ ] Decide whether resume metadata and original normalized text need durable storage; if so, choose Qdrant payloads or a structured database such as PostgreSQL.
- [ ] Deduplicate results by `resumeId` where appropriate, while preserving the best-matching chunks for each resume.
- [ ] Decide how repeated uploads of the same resume should behave (allow separate versions, detect duplicates, or update an existing resume).
- [ ] Add a configurable minimum similarity score.
- [ ] Aggregate chunk-level scores into a candidate-level ranking.
- [ ] Test uploads, matching, and profile retrieval after application restarts.

### Phase 4: LLM-Powered Evaluation

- [ ] Integrate an LLM
- [ ] Evaluate candidates against job requirements
- [ ] Identify matching and missing skills
- [ ] Generate evidence-based explanations grounded in resume text
- [ ] Return structured evaluation results

### Phase 5: Matching Quality and Reliability

- [ ] Improve employment history and project extraction
- [ ] Evaluate matching quality using representative resumes and job descriptions
- [ ] Add automated tests for parsing, embeddings, Qdrant persistence, and matching
- [ ] Add error handling and operational checks for Qdrant and Ollama

## Development Philosophy

The project follows an incremental approach:

1. Build a working semantic matching pipeline.
2. Persist vectors, chunks, and candidate profiles in Qdrant without disrupting matching.
3. Remove redundant in-memory state and make matching robust to duplicate uploads.
4. Add LLM capabilities where they provide meaningful value.
5. Evaluate matching quality before adding unnecessary complexity.

The goal is to build a practical resume-matching system with a clear separation between document processing, embedding generation, retrieval, and candidate evaluation.

## License
--------------**---------