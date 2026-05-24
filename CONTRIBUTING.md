# Contributing to AI Logistics Automation Hub

Thank you for your interest in contributing. This document outlines how to propose, develop, and submit changes to the project.

## Development Philosophy

AI Logistics Automation Hub is a professional-grade Spring Boot backend built around a layered architecture. Contributions should preserve:

- **Separation of concerns** — controllers delegate; services contain business logic; repositories handle persistence.
- **Stateless service layer** — services should not hold mutable state between requests.
- **Explicit dependencies** — all dependencies are injected via constructors, never through fields.
- **Clean API contracts** — DTOs define the shape of all data entering or leaving the application.

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- A valid **Groq API key** (or OpenAI/Gemini key for alternative provider testing)
- A **Slack Incoming Webhook URL** and **Gmail App Password** for notification testing

### Local Setup

```bash
# Clone the repository
git clone https://github.com/HectorCorbellini/logistics-ai-bridge.git
cd logistics-ai-bridge

# Copy and populate environment variables
cp .env.example .env
# Edit .env with your credentials

# Run the application
mvn spring-boot:run

# Run the test suite
mvn test
```

The API documentation is available at `http://localhost:8080/swagger-ui.html` once the application is running.

## Making Changes

### Branching Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable, production-ready code |
| `feature/<short-description>` | New features or non-urgent improvements |
| `fix/<short-description>` | Bug fixes |
| `docs/<short-description>` | Documentation-only changes |

### Workflow

1. **Fork** the repository and create a branch from `main`.
2. **Develop** your change, keeping commits focused and atomic.
3. **Test** locally — all tests must pass before opening a pull request.
4. **Open a Pull Request** against `main`, filling out the PR template with a clear description of the change and its motivation.
5. **Address review feedback** in the same branch; the PR updates automatically.

### Commit Messages

Use imperative mood and a short summary line (under 72 characters). Reference GitHub issues when applicable:

```
feat: add Gemini as an alternative LLM provider

Adds a new GeminiAIService that mirrors the existing Groq integration,
allowing users to swap providers via the GROQ_API_KEY / GEMINI_API_KEY env var.

Closes #42
```

### Code Style

- Follow standard **Java naming conventions** (camelCase for locals/params, PascalCase for types).
- Use **`Locale.US`** for all number and date formatting to ensure consistent output regardless of host environment.
- Do **not** add comments unless explaining non-obvious logic — the code should be self-documenting.
- Annotate public API surfaces with **Swagger `@Operation` / `@ApiResponse`** descriptions.

## Testing

All contributions must pass the existing test suite. New features should include corresponding unit or integration tests.

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=AIServiceTest
```

### Integration Tests

Integration tests make real HTTP calls and, for AI-related tests, real LLM API calls. They are tagged and can be skipped during fast local iteration:

```bash
# Skip integration tests (only runs unit tests)
mvn test -Dtest='!**IntegrationTest'
```

## Reporting Issues

Use [GitHub Issues](https://github.com/HectorCorbellini/ai-logistics-automation-hub/issues) to report:

- **Bugs** — include the full stack trace, the environment (`mvn -version`, OS), and steps to reproduce.
- **Enhancements** — describe the desired behavior and the business context that motivates it.

## Project Structure

```
src/main/java/com/example/apibridge/
├── config/          # Spring configuration beans (OpenAPI, REST template)
├── controller/      # REST endpoints; delegates to services
├── dto/             # Data transfer objects for API contracts
├── exception/       # Custom exceptions and GlobalExceptionHandler
├── mapper/          # JPA entity ↔ DTO mappers
├── model/           # JPA entities (persistence layer)
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic and AI orchestration
└── util/            # Shared utilities (e.g., MessageFormatter)

demo-assets/         # Sample input texts used for live demos
```

## License

By submitting a contribution, you agree that your work will be licensed under the same license as the project.
