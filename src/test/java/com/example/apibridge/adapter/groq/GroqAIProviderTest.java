package com.example.apibridge.adapter.groq;

import com.example.apibridge.adapter.resilience.AIProviderResilienceDecorator;
import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.exception.AIExtractionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroqAIProviderTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ObjectMapper objectMapper = new ObjectMapper();
    private GroqAIProvider groqAIProvider;
    private AIProviderResilienceDecorator resilience;

    private static final String API_URL = "http://mock-groq/v1/chat/completions";
    private static final String API_KEY = "mock-key";
    private static final String MODEL = "mock-model";

    @BeforeEach
    public void setUp() {
        resilience = new AIProviderResilienceDecorator(RetryRegistry.ofDefaults(), CircuitBreakerRegistry.ofDefaults());
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        groqAIProvider = new GroqAIProvider(API_KEY, MODEL, API_URL, webClientBuilder, objectMapper, resilience);
    }

    private void mockWebClient(String response) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(response));
    }

    @Test
    public void testSuccessfulExtraction() {
        String mockResponse = groqResponse("{\"companyName\":\"ACME Corp\",\"date\":\"2023-01-01\",\"totalAmount\":123.45}");
        mockWebClient(mockResponse);

        AIResponse result = groqAIProvider.extract("Invoice from ACME Corp for $123.45 on 2023-01-01");

        assertNotNull(result);
        assertEquals("ACME Corp", result.getCompanyName());
        assertEquals("2023-01-01", result.getDate());
        assertEquals(123.45, result.getTotalAmount());
    }

    @Test
    public void testAdvancedShowcaseExtraction() {
        String mockResponse = groqResponse("{\"companyName\":\"Montevideo Port\",\"status\":\"DELAYED\",\"category\":\"Status Update\",\"isUrgent\":true}");
        mockWebClient(mockResponse);

        AIResponse result = groqAIProvider.extract("Status: DELAYED at Montevideo Port. Urgent.");

        assertEquals("Montevideo Port", result.getCompanyName());
        assertEquals("DELAYED", result.getStatus());
        assertEquals("Status Update", result.getCategory());
        assertTrue(result.getIsUrgent());
    }

    @Test
    public void testStripsJsonMarkdownFence() {
        String fencedContent = "```json\n{\"companyName\":\"Fenced Corp\",\"date\":\"2024-06-01\",\"totalAmount\":500.0}\n```";
        String mockResponse = groqResponseRaw(fencedContent);
        mockWebClient(mockResponse);

        AIResponse result = groqAIProvider.extract("Invoice from Fenced Corp");

        assertEquals("Fenced Corp", result.getCompanyName());
        assertEquals(500.0, result.getTotalAmount());
    }

    @Test
    public void testStripsPlainMarkdownFence() {
        String fencedContent = "```\n{\"companyName\":\"Plain Corp\",\"date\":\"2024-07-01\",\"totalAmount\":99.9}\n```";
        String mockResponse = groqResponseRaw(fencedContent);
        mockWebClient(mockResponse);

        AIResponse result = groqAIProvider.extract("Invoice from Plain Corp");

        assertEquals("Plain Corp", result.getCompanyName());
    }

    @Test
    public void testApiCallFailureThrowsAIExtractionException() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Connection refused")));

        AIExtractionException ex = assertThrows(AIExtractionException.class,
                () -> groqAIProvider.extract("Some invoice text"));
        assertTrue(ex.getMessage().contains("AI Extraction failed"));
    }

    private String groqResponse(String content) {
        return "{\"choices\":[{\"message\":{\"content\":" + JSONObject_quote(content) + "}}]}";
    }

    private String groqResponseRaw(String content) {
        return "{\"choices\":[{\"message\":{\"content\":" + JSONObject_quote(content) + "}}]}";
    }

    private String JSONObject_quote(String string) {
        return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
