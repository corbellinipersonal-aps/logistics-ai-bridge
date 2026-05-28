# Security Status: logistics-ai-bridge

## 🔍 Alignment with Hector Standards
- **Standardized README**: ✅ Updated to reflect current architecture, multi-provider AI, resilience, and security posture.
- **Core Identity**: ✅ Focused on AI-driven logistics automation.
- **Operational Logic Detail**: ✅ High.

## 🛡️ Current Security State
Spring Boot automation system with pluggable AI provider integration (Groq, OpenAI, Gemini).

**Secrets Management:**
- All credentials (API keys, SMTP username/password, Slack webhook URL) are loaded exclusively from environment variables.
- `application.yml` contains **no hardcoded secrets**. Startup will fail fast if a required variable is missing — this is intentional.
- For local development, copy `.env.example` to `.env`, fill in real values, and run via `spring-dotenv` (included as a dependency) or export manually. The `.env` file is listed in `.gitignore` and must never be committed.
- For Docker: `docker run --env-file .env ...`
- For CI/CD: inject secrets via GitHub Actions encrypted secrets or your platform's secret manager.

**Architecture:**
- Full hexagonal boundary — AI providers, persistence, and notifications are each behind a port interface (`AIProvider`, `ExtractionStore`, `NotificationPort`). No framework or vendor types leak into the domain layer.
- Constructor injection used throughout — no hidden field injection.
- **Error Sanitization**: Generic exceptions are sanitized in `GlobalExceptionHandler` to prevent internal stack trace or logic leakage to API consumers.

**Prompt Injection Mitigation:**
- `PromptSanitizer` applies two defensive layers before any user text reaches the LLM:
  1. **Heuristic keyword filter** — rejects input containing known injection phrases (`"ignore all previous instructions"`, `"system override"`, `"jailbreak"`, and others). Returns `400 Bad Request` via `PromptInjectionException`.
  2. **Structural delimiters** — wraps validated text in `<user_input>` XML tags. The system prompt explicitly instructs the model to treat anything inside those tags as data, never as instructions.

**Resilience:**
- All LLM calls are wrapped by `AIProviderResilienceDecorator` (Resilience4j): 3-attempt retry with 1 s back-off, and a circuit breaker that opens after 50 % failure rate over 10 calls. This prevents cascading failures from rate-limits or provider outages.

**SAST/DAST:** Not explicitly configured in this repository.

## 🚀 Security Roadmap
1. ~~**Docker hardening**: Use `eclipse-temurin:17-jre-alpine` to reduce attack surface.~~ ✅ Done
2. ~~**AI input validation**: Sanitize prompts to prevent prompt injection attacks.~~ ✅ Done (`PromptSanitizer`)
3. ~~**Input validation**: `@Valid` + `@NotBlank` constraints on DTOs.~~ ✅ Done
4. **Dependency scanning**: Add GitHub Actions workflow with Snyk or Dependabot to audit Maven libraries.
5. **Rate limiting**: Add per-IP or per-token request throttling on the extraction endpoints to prevent abuse.

---
*This project follows the security standards defined in [hector-repo-standard](https://github.com/HectorCorbellini/hector-repo-standard).*
