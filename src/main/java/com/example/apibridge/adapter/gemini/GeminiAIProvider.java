package com.example.apibridge.adapter.gemini;

import com.example.apibridge.adapter.resilience.AIProviderResilienceDecorator;
import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.exception.AIExtractionException;
import com.example.apibridge.port.AIProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Google Gemini adapter for {@link AIProvider}.
 * Activate by setting {@code ai.provider=gemini} in application.yml.
 * Requires {@code gemini.api.key} and optionally {@code gemini.model}.
 *
 * Uses the Gemini generateContent REST API:
 * POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIProvider.class);

    private final String apiKey;
    private final String model;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AIProviderResilienceDecorator resilience;

    public GeminiAIProvider(@Value("${gemini.api.key}") String apiKey,
                             @Value("${gemini.model:gemini-1.5-flash}") String model,
                             @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}") String apiBaseUrl,
                             WebClient.Builder webClientBuilder,
                             ObjectMapper objectMapper,
                             AIProviderResilienceDecorator resilience) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = webClientBuilder.baseUrl(apiBaseUrl).build();
        this.objectMapper = objectMapper;
        this.resilience = resilience;
    }

    @Override
    public AIResponse extract(String prompt) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ArrayNode parts = contents.addObject().putArray("parts");
        parts.addObject().put("text",
                "You are a specialized data extraction assistant. You always respond with pure JSON, no conversational text.\n\n"
                        + prompt);

        return resilience.execute(() -> doExtract(requestBody));
    }

    private AIResponse doExtract(ObjectNode requestBody) {
        String endpoint = "/" + model + ":generateContent?key=" + apiKey;
        try {
            log.info("Calling Gemini API via WebClient (Model: {})...", model);

            String response = webClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new AIExtractionException("Gemini API error: " + errorBody)))
                    )
                    .bodyToMono(String.class)
                    .block();

            log.info("Gemini API call successful.");

            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(response);
            String content = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
            content = stripMarkdownFences(content);

            AIResponse result = objectMapper.readValue(content, AIResponse.class);
            log.info("Successfully extracted data for company: {}", result.getCompanyName());
            return result;
        } catch (Exception e) {
            log.error("Gemini extraction failed: {}", e.getMessage());
            throw new AIExtractionException("AI Extraction failed: " + e.getMessage(), e);
        }
    }

    private static String stripMarkdownFences(String content) {
        if (content.startsWith("```json")) content = content.substring(7);
        else if (content.startsWith("```")) content = content.substring(3);
        if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
        return content.trim();
    }
}
