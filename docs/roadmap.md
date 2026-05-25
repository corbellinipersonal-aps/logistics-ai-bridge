# Project Roadmap

## Goal
Connect logistics data streams to modern communication tools (Slack, Email) by extracting actionable insights from unstructured text.

## Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3
- **Database**: H2 (Persistent)
- **Build Tool**: Maven
- **Integrations**: Groq AI, Slack Webhooks, SMTP Email

---

## Progress Tracking

### Phase 1: Core Implementation ✅
- [x] Analyze output formats and DTO requirements.
- [x] Implement `ExtractionFetchService`.
- [x] Implement `EmailSenderService` and `SlackSenderService`.
- [x] Expose REST endpoints for notification dispatch.
- [x] Integrate AI layer (`AIService`) for real-time extraction.
- [x] Add Global Exception Handling and logging.
- [x] Implement Dockerization.

### Phase 2: Security & Configuration ✅
- [x] Implement Environment Variable strategy for secrets.
- [x] Migrate to `application.yml`.
- [x] Ensure robust `.gitignore` coverage.

### Phase 3: Logistics Showcase Scenarios ✅
- [x] **Scenario 1**: Supplier Invoice Routing.
- [x] **Scenario 2**: Status Updates & Delay Alerts.
- [x] **Scenario 3**: Operations Insights & Summarization.
- [x] Create interactive `demo-guide.md` showcase.

### Phase 4: Strategic Polish ✅
- [x] Professionalize documentation and architectural guides.
- [x] Standardize internal logs and review notes.

### Phase 5: Demo Preparation (Video Readiness) ✅
- [x] **Happy Path Polish**: Refined `/api/send/ai/extract` for edge-case handling.
- [x] **Observability**: Implemented a glassmorphic dashboard (`/index.html`) for real-time logging feedback.
- [x] **Demo Data**: Created a population utility (`/api/demo/populate`) for rapid scenario resets.

### Phase 6: Promotional Video Production ✅
- [x] **Visual Assets**: Successfully generated automated showcase clips using Playwright.
- [x] **Automated Audio**: Implemented TTS generation and FFmpeg mixing pipeline.
- [x] **Final Sync**: Resolved voice overlaps using naturally-paced narration blocks.
- [x] **Production Ready**: Generated `final_showcase_with_audio.webm` with synchronized narration.
- [ ] **Marketing**
    - [x] Publish narrated demo on YouTube ([youtu.be/TULulfYLYKE](https://youtu.be/TULulfYLYKE)).
    - [ ] Design high-impact thumbnail (YouTube / README).
    - [ ] Global rollout (LinkedIn/Discord).

### Phase 7: Architectural Evolution (In Progress)
- [x] **AI provider port**: `AIProvider` interface + `GroqAIProvider` adapter (`WebClient`).
- [x] **Service decoupling**: `AIService` delegates extraction HTTP to the port (prompt logic remains in the service).
- [ ] **Notification & persistence ports**: Decouple email, Slack, and JPA behind adapters (full hexagonal boundary).
- [ ] **Multi-provider support**: OpenAI and Gemini `AIProvider` implementations + config switch.
- [ ] **Self-hosted / enterprise LLM**: Optional adapter for OpenAI-compatible on-prem APIs (Ollama, vLLM, etc.).
- [ ] **Advanced integrations**: Notion, Discord, and scheduled digests.
