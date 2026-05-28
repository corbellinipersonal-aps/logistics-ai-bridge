package com.example.apibridge.adapter.groq;

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

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq", matchIfMissing = true)
public class GroqAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(GroqAIProvider.class);

    private final String groqApiKey;
    private final String groqModel;
    private final String groqApiUrl;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AIProviderResilienceDecorator resilience;

    public GroqAIProvider(@Value("${groq.api.key}") String groqApiKey,
            @Value("${groq.model}") String groqModel,
            @Value("${groq.api.url}") String groqApiUrl,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            AIProviderResilienceDecorator resilience) {
        this.groqApiKey = groqApiKey;
        this.groqModel = groqModel;
        this.groqApiUrl = groqApiUrl;
        this.webClient = webClientBuilder.baseUrl(groqApiUrl).build();
        this.objectMapper = objectMapper;
        this.resilience = resilience;
    }

    @Override
    public AIResponse extract(String prompt) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", groqModel);

        ArrayNode messages = requestBody.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content",
                        "You are a specialized data extraction assistant. You always respond with pure JSON, no conversational text.");
        messages.addObject()
                .put("role", "user")
                .put("content", prompt);

        return resilience.execute(() -> doExtract(requestBody));
    }

    private AIResponse doExtract(ObjectNode requestBody) {
        try {
            log.info("Calling Groq API via WebClient (Model: {})...", groqModel);

            String response = webClient.post()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new AIExtractionException("Groq API error: " + errorBody)))
                    )
                    .bodyToMono(String.class)
                    .block();

            log.info("Groq API call successful.");

            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(response);
            com.fasterxml.jackson.databind.JsonNode choices = rootNode.path("choices");
            if (choices.isEmpty()) {
                throw new AIExtractionException("Groq API returned no choices (possible content filter)");
            }
            String content = choices.get(0).path("message").path("content").asText();
            content = stripMarkdownFences(content);

            AIResponse result = objectMapper.readValue(content, AIResponse.class);
            log.info("Successfully extracted data for company: {}", result.getCompanyName());
            return result;
        } catch (Exception e) {
            log.error("Groq extraction failed: {}", e.getMessage());
            throw new AIExtractionException("AI Extraction failed: " + e.getMessage(), e);
        }
    }

    static String stripMarkdownFences(String content) {
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }
}
