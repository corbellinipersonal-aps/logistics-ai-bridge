# Contributing to AI Logistics Automation Hub

Thank you for your interest in contributing. This document outlines how to propose, develop, and submit changes to the project.

For strategic context and architectural direction, see [`ANALYSIS.md`](ANALYSIS.md). For release history, see [`CHANGELOG.md`](CHANGELOG.md). For phased goals and backlog, see [`docs/roadmap.md`](docs/roadmap.md).

## Development Philosophy

AI Logistics Automation Hub is a professional-grade Spring Boot backend built around a **full hexagonal (ports-and-adapters) architecture**. Contributions should preserve:

- **Separation of concerns** — controllers delegate; services contain business logic; adapters handle all external I/O.
- **Port boundaries** — LLM calls go through `AIProvider`, persistence through `ExtractionStore`, notifications through `NotificationPort`. Do not add provider-specific or framework-specific code inside services.
- **Provider decoupling** — select Groq, OpenAI, or Gemini via `ai.provider` in `application.yml`; no code changes required.
- **Stateless service layer** — services should not hold mutable state between requests.
- **Explicit dependencies** — all dependencies are injected via constructors, never through fields.
- **Clean API contracts** — DTOs define the shape of all data entering or leaving the application.
- **Security by default** — user input must pass through `PromptSanitizer` before reaching the LLM; new endpoints should validate input with `@Valid`.

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- A valid **Groq API key** (or OpenAI/Gemini key if switching providers — see `application.yml`)
- A **Slack Incoming Webhook URL** and **Gmail App Password** (optional, for notification testing)

### Local Setup

```bash
# Clone the repository
git clone https://github.com/corbellinipersonal-aps/logistics-ai-bridge.git
cd logistics-ai-bridge

# Copy and populate environment variables
cp .env.example .env
# Edit .env with your credentials

# Run the application
mvn spring-boot:run

# Run with demo endpoints enabled (/api/demo/*)
mvn spring-boot:run -Dspring-boot.run.profiles=demo

# Run the test suite
mvn test
```

The API documentation is available at `http://localhost:8080/swagger-ui/index.html` once the application is running.

## Making Changes

### Branching Strategy

| Branch | Purpose |
|--------|---------|
| `master` | Stable, production-ready code (default) |
| `feature/<short-description>` | New features or non-urgent improvements |
| `fix/<short-description>` | Bug fixes |
| `docs/<short-description>` | Documentation-only changes |

### Workflow

1. **Fork** the repository and create a branch from `master`.
2. **Develop** your change, keeping commits focused and atomic.
3. **Test** locally — all tests must pass before opening a pull request.
4. **Open a Pull Request** against `master`, with a clear description of the change and its motivation.
5. **Address review feedback** in the same branch; the PR updates automatically.

CI runs on push and pull requests to `master` and `main` via [`.github/workflows/maven.yml`](.github/workflows/maven.yml).

### Commit Messages

Use imperative mood and a short summary line (under 72 characters). Reference GitHub issues when applicable:

```
feat: add Gemini adapter implementing AIProvider

Introduces GeminiAIProvider using WebClient, selectable via configuration
without changing AIService prompt logic.

Closes #42
```

### Code Style

- Follow standard **Java naming conventions** (camelCase for locals/params, PascalCase for types).
- Use **`Locale.US`** for all number and date formatting to ensure consistent output regardless of host environment.
- Do **not** add comments unless explaining non-obvious logic — the code should be self-documenting.
- Annotate public API surfaces with **Swagger `@Operation` / `@ApiResponse`** descriptions.
- Prefer **`WebClient`** for new outbound HTTP integrations (see `GroqAIProvider`); avoid introducing new `RestTemplate` usage.

## Testing

All contributions must pass the existing test suite. New features should include corresponding unit or integration tests.

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=AIServiceTest
```

### Integration Tests

Integration tests may make real HTTP calls and, for AI-related tests, real LLM API calls. They can be skipped during fast local iteration:

```bash
# Skip integration tests (only runs unit tests)
mvn test -Dtest='!**IntegrationTest'
```

## Reporting Issues

Use [GitHub Issues](https://github.com/corbellinipersonal-aps/logistics-ai-bridge/issues) to report:

- **Bugs** — include the full stack trace, the environment (`mvn -version`, OS), and steps to reproduce.
- **Enhancements** — describe the desired behavior and the business context that motivates it.

## Project Structure

```
src/main/java/com/example/apibridge/
├── adapter/
│   ├── gemini/          # GeminiAIProvider
│   ├── groq/            # GroqAIProvider
│   ├── notification/    # EmailNotificationAdapter, SlackNotificationAdapter
│   ├── openai/          # OpenAIProvider
│   ├── persistence/     # JpaExtractionStore
│   └── resilience/      # AIProviderResilienceDecorator (Resilience4j)
├── config/              # Spring beans (OpenAPI, WebClient, Slack, ObjectMapper)
├── controller/          # REST endpoints; delegates to services
├── demo/                # Demo profile utilities (populate, reset)
├── dto/                 # Data transfer objects for API contracts
├── exception/           # Custom exceptions and GlobalExceptionHandler
├── mapper/              # JPA entity ↔ DTO mappers
├── model/               # JPA entities (persistence layer)
├── port/                # AIProvider, ExtractionStore, NotificationPort
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic and orchestration (AIService, ExtractionService)
└── util/                # Shared utilities (MessageFormatter, PromptSanitizer)

demo-assets/         # Sample input texts used for live demos
docs/                # Integration guide, demo guide, roadmap, troubleshooting
```

## License

By submitting a contribution, you agree that your work will be licensed under the same license as the project.
