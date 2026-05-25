# Project Analysis

> **Strategic analysis** — Reconstructs the intended contents of the missing `Strategy.md`, inferred from project documentation and the AUTOM-HUB archive (`CUIDADO-RECORDAR`, `hector-repo-standard`).  
> For release history and shipped changes, see [`CHANGELOG.md`](CHANGELOG.md). For phase checklists and future work, see [`docs/roadmap.md`](docs/roadmap.md).

---

# Strategy

## Vision

**AI Logistics Automation Hub** (`logistics-ai-bridge`) is a portfolio-grade backend that turns unstructured logistics text (invoices, shipment emails, delay notices) into validated JSON and routes it to the channels operations teams already use (REST, email, Slack).

The strategic goal is not “another CRUD API” but a **credible demonstration** of:

- Production-style **LLM integration** with strict JSON contracts
- **Clean, evolvable architecture** (ports/adapters toward hexagonal design)
- **End-to-end delivery** (API, persistence, notifications, dashboard, demo video, CI)

## Problem Statement

Logistics and back-office teams receive critical data in free-form text. Manual copy-paste into spreadsheets or ERP systems is slow, error-prone, and does not scale. This project automates the first mile: **read → extract → persist → notify**.

## Target Users

| Audience | What they need from this project |
|---|---|
| **Hiring managers / reviewers** | Evidence of Java 17, Spring Boot 3, AI integration, testing, Docker, and documentation discipline |
| **Technical evaluators** | Clear API surface, Swagger, reproducible demo scenarios |
| **Future contributors** | Extensible provider model (Groq today; OpenAI/Gemini planned) |

## Strategic Pillars

### 1. Reliability over novelty

- LLM output must map to a **fixed schema** (`companyName`, `date`, `totalAmount`, `category`, `status`, `isUrgent`).
- Prompts enforce **JSON-only** responses; adapters strip markdown fences and validate parsing.
- Failures surface as structured errors (`GlobalExceptionHandler`, `AIExtractionException`), not silent `null` returns.

### 2. Pragmatic architecture, path to hexagonal

**Current state (hybrid layered + partial ports):**

- `AIProvider` port + `GroqAIProvider` adapter — **done** (core extraction decoupled from Groq).
- `AIService` owns prompt construction — **appropriate** application logic.
- Email (`EmailSenderService`), Slack (`SlackSenderService`), and JPA persistence remain **directly coupled** to Spring/mail/Slack SDK — **intentional deferral** (see AUTOM-HUB note: full hexagonal refactor is non-trivial).

**Target state:**

```
Domain (extraction rules, validation) — no Spring/Groq/H2/Slack imports
    ↑ ports: AIProvider, NotificationPort, ExtractionRepositoryPort
    ↓ adapters: Groq, OpenAI, Gemini, SMTP, Slack, H2/JPA
```

### 3. Showcase-first delivery

Three logistics narratives drive demos and documentation:

1. **Supplier invoice routing** — parse invoice text → structured amounts and vendor.
2. **Status updates & delay alerts** — flag `Delayed` / `isUrgent` for operations.
3. **Operations digest** — consolidate multi-entry text into one summary JSON.

Supporting assets: `demo-assets/`, `docs/demo-guide.md`, `/index.html` dashboard, `video-recorder/` pipeline.

### 4. Security & operability by default

- Secrets via environment variables (`.env` / `application.yml`), never committed.
- `.gitignore` excludes `target/`, `h2_data/`, `.env`, `node_modules/`.
- Docker + Maven CI for repeatable builds.
- GitHub topics for discoverability (`java`, `spring-boot`, `llm`, `document-processing`, etc.).

## Technology Strategy

| Layer | Choice | Rationale |
|---|---|---|
| Runtime | Java 17 | Aligns with portfolio positioning and enterprise expectations |
| Framework | Spring Boot 3 | Mature ecosystem, JPA, mail, validation, OpenAPI |
| AI (active) | Groq / Llama 3.1 | Fast, cost-effective for demos; `WebClient` for non-blocking calls |
| AI (planned) | OpenAI, Gemini | Same `AIProvider` port; swap adapters via configuration |
| Persistence | H2 (file) | Zero-setup evaluation; swap to PostgreSQL for production narrative |
| Notifications | SMTP + Slack webhook | Realistic ops workflows without heavy infra |
| API docs | springdoc / Swagger | Low-friction reviewer testing |
| Frontend | Static dashboard | Visual proof without a separate SPA repo |

## Go-to-Market Strategy (Portfolio)

| Channel | Action | Status |
|---|---|---|
| GitHub | Polished README, topics, CI badge, demo screenshot | Done |
| Video | [YouTube demo](https://youtu.be/TULulfYLYKE) (+ local pipeline) | Done |
| LinkedIn / Discord | Short post + link to repo and video | Pending (Phase 6 marketing) |
| Profile README | Feature as “Intelligent Data Extractor” | Done on [HectorCorbellini](https://github.com/HectorCorbellini) |

## Risk Register & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| LLM returns invalid JSON | Extraction fails | Strict prompts; adapter parsing; tests on `GroqAIProvider` |
| API key leakage | Security incident | `.env` gitignored; rotate keys; remove token from git remote URL |
| Over-claiming “hexagonal” | Credibility loss | Document hybrid state honestly; roadmap Phase 7 |
| H2 in production narrative | Scalability questions | Document as demo DB; note PostgreSQL migration path |
| Provider lock-in to Groq | Vendor risk | `AIProvider` interface; planned OpenAI/Gemini adapters |

## Success Criteria

- [x] `mvn test` passes in CI
- [x] `/api/send/ai/extract` returns valid JSON for demo scenarios
- [x] Extractions persist and list via `/api/extractions`
- [x] Email and Slack paths work when credentials configured
- [x] Repo public, documented, containerized
- [ ] Full hexagonal refactor (notification + persistence ports)
- [ ] Multi-provider configuration switch
- [ ] Marketing rollout (thumbnail, social posts)
