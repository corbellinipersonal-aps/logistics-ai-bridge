# Changelog

All notable changes to this project will be documented in this file.

## [1.8.0] - 2026-05-28

### Security
- **Prompt injection mitigation**: Introduced `PromptSanitizer` with two defensive layers applied before any user text reaches the LLM:
  - Heuristic keyword filter — rejects input containing known injection phrases (`"ignore all previous instructions"`, `"system override"`, `"jailbreak"`, and 12 others) with a `PromptInjectionException`.
  - Structural delimiters — wraps validated text in `<user_input>` XML tags; the system prompt explicitly instructs the model to treat anything inside those tags as data, never as instructions.
- **`PromptInjectionException`**: New exception type mapped to `400 Bad Request` in `GlobalExceptionHandler`.
- **`AIService` hardened**: `buildPrompt()` now routes all user text through `PromptSanitizer.sanitize()` before embedding it in the prompt.

## [1.7.0] - 2026-05-28

### Added
- **Multi-provider AI support**: Added `OpenAIProvider` and `GeminiAIProvider` adapters implementing the existing `AIProvider` port. Switch providers with a single line in `application.yml` (`ai.provider: groq | openai | gemini`). Each adapter is gated with `@ConditionalOnProperty` so only the selected one is loaded.
- **Resilience4j retry + circuit breaker**: Introduced `AIProviderResilienceDecorator`, a shared Spring component that wraps every AI provider call with a configurable retry (3 attempts, 1 s back-off) and circuit breaker (opens at 50 % failure rate over 10 calls, 30 s wait). All three adapters delegate to it via `resilience.execute(() -> doExtract(...))`.
- **Resilience config in `application.yml`**: Added `resilience4j.retry` and `resilience4j.circuitbreaker` blocks under the `ai-provider` instance name — thresholds are tunable without code changes.

### Changed
- `GroqAIProvider` now uses `AIProviderResilienceDecorator` and splits the HTTP call into a private `doExtract` method, keeping `extract` clean.
- `GroqAIProvider` annotated with `@ConditionalOnProperty(matchIfMissing = true)` so it remains the default when `ai.provider` is not set.

### Tests
- `GroqAIProviderTest` updated to pass a real `AIProviderResilienceDecorator` backed by default in-memory registries.

## [1.6.0] - 2026-05-28

### Changed
- **Notification ports/adapters**: Introduced `NotificationPort` interface and moved all email/Slack logic out of the service layer into dedicated adapters — `EmailNotificationAdapter` and `SlackNotificationAdapter`. `EmailSenderService` and `SlackSenderService` have been removed.
- **Persistence port/adapter**: Introduced `ExtractionStore` interface and moved all JPA/Hibernate details into `JpaExtractionStore`. `ExtractionService` now depends solely on the port, with no framework types in its import list.
- **SendController decoupled**: `SendController` now injects `NotificationPort` by qualifier (`email` / `slack`) instead of concrete service classes.
- **Dockerfile fix**: Corrected the `COPY` path from `autom-hub-0.0.1-SNAPSHOT.jar` to `apibridge-0.0.1-SNAPSHOT.jar` to match the Maven `artifactId`.
- **Dead bean removed**: Removed the unused `RestTemplate` bean and its import from `RestConfig`.

### Tests
- `ExtractionServiceTest` updated to mock `ExtractionStore` instead of `ExtractionRepository` + `ExtractionMapper`.
- `SlackSenderServiceTest` replaced by `SlackNotificationAdapterTest` under the adapter package.
- `SendControllerAIIntegrationTest` and `SendControllerIntegrationTest` updated to mock/stub `NotificationPort` beans by qualifier.

## [1.5.0] - 2026-05-27

### Changed
- **Architectural Decoupling**: Successfully extracted the `video-recorder` Node.js/Playwright pipeline into its own standalone repository: [dev-video-automation](https://github.com/HectorCorbellini/dev-video-automation). This move eliminates the "Accidental Monolith" pattern and separates backend business logic from media/automation infrastructure.
- **Project Hygiene**: Removed `node_modules` references and video artifacts from the Java project's `.gitignore` and root structure.
- **Documentation Migration**: Relocated `RECORDING.md` to the new repository and updated the root `README.md` to reference the external "Dev-Video-Pipeline" for local showcase reproduction.

### Verification
- **Post-Decoupling Verification**: Successfully executed the complete test suite (31 tests passing) and validated the application's runtime usage. Real-time extraction against Groq API, data population (`/api/demo/populate`), and mock SMTP/Slack dispatches via `verify_usage.sh` remain fully operational in the stripped-down repository.

## [1.4.0] - 2026-05-27

### Added
- **Strategic Analysis Document**: Introduced `ANALYSIS.md` with portfolio strategy, architecture direction, and success criteria (release history remains in this changelog; phase tracking in `docs/roadmap.md`).
- **README Business Impact Section**: Added an outcome-focused summary for non-technical reviewers (manual re-keying reduction, faster ops response, adaptable AI backend).
- **Customization & Extensibility Section**: Documented the `AIProvider` port, `GroqAIProvider` adapter, and planned OpenAI/Gemini/self-hosted options with honest scope boundaries.
- **Expanded “Work with Me” CTA**: Dual audience call-to-action for employers and business/consulting inquiries, linked to LinkedIn and `ANALYSIS.md`.
- **YouTube Demo Publishing**: Embedded a clickable YouTube thumbnail and link in the README ([youtu.be/TULulfYLYKE](https://youtu.be/TULulfYLYKE)); updated `RECORDING.md` with the published URL.

### Changed
- **AI Integration Guide Rewrite**: Updated `docs/ai-integration.md` to reflect the current port/adapter design (`AIProvider`, `GroqAIProvider`, `WebClient`) and steps for adding new LLM backends.
- **Roadmap Phase 7 Refresh**: Marked `AIProvider` and `GroqAIProvider` as complete; split remaining hexagonal work (notification/persistence ports, multi-provider config) into explicit checklist items; recorded YouTube publication under marketing.
- **Contributing Guide Alignment**: Updated `CONTRIBUTING.md` for `master` as default branch, demo profile, `WebClient` conventions, corrected Swagger/Issues URLs, and expanded project structure (`port/`, `adapter/`, `demo/`).
- **README Architecture Table**: Replaced generic layered description with ports-and-adapters terminology aligned to the codebase.

## [1.3.0] - 2026-05-24

### Added
- **WebClient Integration**: Migrated `GroqAIProvider` from `RestTemplate` to the modern, non-blocking `WebClient` for AI extractions, improving scalability and error handling.
- **Enhanced Configuration Resilience**: Added default values for all critical environment variables in `application.yml`, preventing startup failures when secrets are not present.

### Changed
- **CI/CD Reliability**: Updated the Maven GitHub Actions workflow to trigger on both `main` and `master` branches, ensuring consistent CI coverage.
- **Project Naming Consistency**: Aligned the Maven `artifactId` (`apibridge`) with the base Java package name for a cleaner project structure.

### Security
- **Generic Exception Sanitization**: Refactored `GlobalExceptionHandler` to prevent internal data leakage by masking raw exception messages in the generic 500 error response.

### Fixed
- **Startup Warnings**: Resolved the redundant Hibernate H2 Dialect warning by removing explicit configuration in `application.yml`, allowing for automatic dialect detection.

## [1.2.3] - 2026-04-27

### Added
- **Video Pipeline Pre-flight Checks**: `record.js` now validates both server availability and `demo` Spring profile activation before launching Playwright, aborting with an actionable error message if either check fails.
- **Audio Duration Validation**: `merge_video_audio.js` uses `ffprobe` to measure each audio block's real duration and aborts the FFmpeg merge with a clear error if any block would overlap the next scene's start timestamp.
- **Convenience npm Scripts**: Added `generate-audio`, `record`, `merge`, and `build-video` scripts to `video-recorder/package.json` for single-command pipeline execution.

### Changed
- **Demo Profile Gating**: `DemoController` is now annotated with `@Profile("demo")`, ensuring `/api/demo/reset` and `/api/demo/populate` endpoints are completely absent from production deployments. Start with `mvn spring-boot:run -Dspring-boot.run.profiles=demo` to enable them.
- **Video-Recorder Dependency Isolation**: Moved `gtts` dependency from the root `package.json` into `video-recorder/package.json`. The root `package.json` and `package-lock.json` have been removed so video tooling no longer leaks into the Java project root.
- **H2 Persistence**: Switched datasource URL from `jdbc:h2:mem:testdb` (in-memory) to `jdbc:h2:file:./h2_data/testdb` (file-based) to ensure extraction records survive server restarts and multi-session demo recordings.
- **Duplicate Audio Script Removed**: Deleted `video-recorder/generate_audio.py` (Python/gTTS) to establish `generate_audio.js` as the single source of truth for narration text and TTS generation.
- **Orphan Artefact Removed**: Deleted `video-recorder/videos/list.txt`, a leftover from a previous `ffmpeg -f concat` approach that was no longer referenced anywhere.
- **Recording Timing Aligned to Audio**: Extended `waitForTimeout` durations in `record.js` to produce a ~60-second video, matching the timestamp offsets expected by `merge_video_audio.js` audio blocks.
- **Video File Naming Fixed**: `record.js` now captures `page.video().path()` after context close and renames the Playwright-generated random-UUID `.webm` to the canonical `final_showcase.webm` expected by `merge_video_audio.js`.
- **RECORDING.md Accuracy**: Corrected documentation that incorrectly claimed `atempo` was used; updated to reflect the current approach of naturally-paced, pre-shortened audio blocks.

### Fixed
- **`AIResponse` Unknown Field Crash**: Added `@JsonIgnoreProperties(ignoreUnknown = true)` to `AIResponse`, preventing `UnrecognizedFieldException` when the LLM includes extra fields (e.g., `additionalInfo`) beyond the defined DTO schema. Demo now reliably creates 5/5 records.
- **Nested Output Directory Bug**: Removed `generate_audio.py` which used a relative path `video-recorder/videos/audio_blocks` and would silently create a nested `video-recorder/video-recorder/videos/` directory if run from inside the `video-recorder/` folder.
- **Final Audio Block Overlap**: Shortened the `block_55` narration text so its TTS output fits within the 5-second slot available before end-of-video.

### Security / Hygiene
- **`.gitignore` Hardened**: Added `node_modules/`, `h2_data/`, and `video-recorder/videos/` to prevent build artefacts, database files, and generated media from being accidentally committed.

## [1.2.2] - 2026-04-26

### Added
- **Intelligent Audio Overlap Prevention**: Enhanced the `merge_video_audio.js` script with FFmpeg `atempo` filters to automatically compress narration blocks that exceed their visual scene duration.
- **Automated Narration Pipeline**: Fully automated the generation of voiceovers and their synchronization with the video showcase.

### Changed
- **Production Documentation**: Updated `RECORDING.md` and `NARRATION_SCRIPT.md` to document the automated audio pacing logic.

### Added
- **Automated Video Showcase**: Implemented and executed a Playwright-based recording script (`record.js`) to generate a professional .webm showcase, covering the dashboard, Swagger UI extraction, and real-time population scenarios.
- **Roadmap Update**: Marked the Video Production phase as completed in `roadmap.md`.

### Changed
- **Architectural Refactoring**: Renamed `ExtractionFetchService` to `ExtractionService` across the entire codebase (including controllers and tests) to better reflect its comprehensive role in handling both data persistence and retrieval.
- **Service-Layer Documentation**: Added detailed Javadoc to `ExtractionService` and `AIService` to formally define their architectural boundaries (Persistence vs. Orchestration logic).

### Fixed
- **Naming Ambiguity**: Resolved the misleading "fetch-only" connotation of the service layer by adopting a more neutral and accurate naming convention.

## [1.1.2] - 2026-04-25

### Added
- **Video Production Roadmap**: Restored the "Promotional Video Production" phase (Phase 6) to `roadmap.md`, including the 0:00-1:00 recording script and marketing strategy.
- **Showcase Milestones**: Re-included "Demo Preparation" (Phase 5) in the roadmap to track dashboard observability and demo-data population utilities.

### Added
- **AI Extraction Prompt Optimization**: Refined the system prompt in `AIService` to enforce flat JSON output, specifically resolving the 4/5 record creation failure during demo population of complex operation logs.

### Fixed
- **SMTP Authentication Compliance**: Secured the email integration by migrating to a Google App Password, ensuring reliable SMTP authentication and better account protection.

## [1.1.0] - 2026-04-25

### Added
- **Professional Documentation Suite**: Consolidated all project metadata into a clean `docs/` directory, including a new `roadmap.md`, `ai-integration.md`, `demo-guide.md`, `troubleshooting.md`, and `review-notes.md`.
- **Internal Development Logs**: Moved session-specific logs and developer notes to `.github/INTERNAL_LOGS.md` to maintain a professional, visitor-facing `docs/` folder.
- **Logistics Showcase Scenarios**: Implemented advanced extraction scenarios for Supplier Invoices, Shipment Delays, and Daily Ops Logs.
- **Enhanced AI Model**: Expanded `AIResponse` DTO with `status`, `category`, and `isUrgent` fields to support specialized logistics logic.
- **Advanced Prompting**: Upgraded `AIService` system prompts to perform intelligent categorization and urgency detection.
- **Rich Assets**: Added `invoice-routing.txt`, `status-delay.txt`, and `logistics-summary.txt` to `demo-assets/` for rapid stakeholder demonstrations.
- **Storytelling Documentation**: Created `demo-guide.md` (previously `DEMO.md`) which translates technical features into business-value stories with runnable examples.
- **Urgency-Aware Notifications**: Updated `MessageFormatter` and notification services to visually flag urgent extraction results with clear warnings.
- **Comprehensive Testing**: Added `testAdvancedShowcaseExtraction` to `AIServiceTest` and verified entire 31-test suite success.
- **Architectural Cleanup**: Standardized constructor injection by removing redundant `@Autowired` annotations from all Controllers and Services, aligning with modern Spring Boot standards.
- **Robust Data Mapping**: Restored `@JsonProperty("totalAmount")` in `AIResponse` DTO to ensure reliable deserialization from LLM responses regardless of casing.
- **Observability Dashboard**: Developed a real-time, glassmorphic monitoring dashboard (`/index.html`) featuring semantic status badges and live extraction statistics.
- **Enhanced Data Persistence**: Updated the `Extraction` entity, DTOs, and Mapper to fully persist and expose new logistics fields (`status`, `category`, `isUrgent`).
- **Semantic Status UI**: Implemented an intelligent status-to-color mapping system in the dashboard (Delivered → Green, Pending → Yellow, Delayed → Orange) for instant visual feedback.
- **New Retrieval API**: Introduced `ExtractionController` to provide a dedicated endpoint for fetching stored extraction records.
- **Notification Consistency**: Unified all notification headers to "Logistics Data Extraction" across AI and database-driven workflows for a professional, cohesive user experience.
- **Demo Data Loader Endpoint**: Added `/api/demo/populate` to process all `.txt` files from `demo-assets/` through the AI extraction pipeline and persist results for live demos.
- **Interactive Dashboard Controls**: Added "Run Demo Scenarios" and "Reset Database" actions to `static/index.html` for one-click showcase flows.
- **Critical Demo Asset**: Added `demo-assets/urgent-critical.txt` to exercise urgency/status behavior with a high-priority incident scenario.
- **JSON Contract Probe Endpoint**: Added `GET /api/extractions/sample` to verify frontend JSON shape compatibility without requiring live AI calls.

### Changed
- **Swagger UI Standardization**: Standardized all Swagger UI references to the consistent `/swagger-ui/index.html` path and updated `application.yml` to ensure technical alignment.
- **README Transparency**: Refined the README to clearly distinguish between the active Groq integration and planned OpenAI/Gemini support, including updated architecture diagrams.
- **Project Metadata Accuracy**: Corrected malformed tags in `pom.xml` and updated the project URL to the official repository.
- **Roadmap Evolution**: Updated roadmap documentation to include Phase 4 (Showcase Scenarios) as a completed strategic requirement.
- **Unified Notification Formatting**: Refactored `MessageFormatter` so both the AI path and the DB retrieval path use a single `format()` method, ensuring `category`, `status`, and `isUrgent` are always included in email/Slack messages regardless of how data was sourced.
- **Exception Handler Ordering**: Reordered handlers in `GlobalExceptionHandler` from most-specific to most-generic (`MethodArgumentNotValidException` → `ResourceNotFoundException` → `NotificationException` → `IllegalArgumentException` → `RuntimeException` → `Exception`) to eliminate the misleading dead-path appearance and align with Spring convention.
- **Testable Architecture Refactoring**: Refactored `SlackSenderService` to use constructor-based dependency injection for the `Slack` client, decoupling it from static factory methods and enabling total isolation through Mockito.
- **Improved Test Stability**: Fixed a locale-dependent formatting bug in `MessageFormatter` by enforcing `Locale.US`, ensuring consistent number formatting (decimal separators) regardless of the host environment.
- **Performance Optimization**: Refactored `SlackSenderService` to initialize the Slack client once in the constructor, preventing resource leaks and aligning its lifecycle with the Spring bean.
- **Architectural Refactoring**: Decoupled `SendController` from persistence logic by moving repository and mapper dependencies into `ExtractionFetchService`, strictly adhering to layered architecture principles.
- **Professional Documentation Upgrade**: Re-structured `README.md` for better positioning and accurate showcases.
- **Architectural Evolution Plan**: Updated roadmap documenting the transition toward a provider-agnostic Hexagonal Architecture.
- **AI-Agnostic Vision**: Formally included OpenAI and Gemini as future supported providers in project goals.
- **Production Container Hardening**: Updated Docker base image from `eclipse-temurin:17-jdk-alpine` to `eclipse-temurin:17-jre-alpine` to reduce runtime image size and attack surface.
