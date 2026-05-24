package com.example.apibridge.service;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionRequest;
import com.example.apibridge.port.AIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Application service that orchestrates AI extraction.
 * <p>
 * Builds domain prompts and delegates provider-specific HTTP calls to {@link AIProvider}
 * adapters, keeping this layer independent of Groq/OpenAI/Gemini implementations.
 * </p>
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final AIProvider aiProvider;

    public AIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    public AIResponse extractData(ExtractionRequest request) {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            log.error("Aborting: Input text is empty or request is null");
            throw new IllegalArgumentException("Input text cannot be empty for AI extraction.");
        }

        log.info("Starting AI extraction for text length: {}", request.getText().length());
        return aiProvider.extract(buildPrompt(request.getText()));
    }

    private String buildPrompt(String text) {
        return "Extract data from the following logistics text. Respond ONLY with a valid JSON object containing the fields: 'companyName', 'date' (YYYY-MM-DD), 'totalAmount' (numeric), 'category' (e.g., Invoice, Status Update), 'status' (e.g., Delayed, Delivered, Pending), and 'isUrgent' (boolean).\n\n"
                + "IMPORTANT: If the text contains multiple entries, consolidate the information into a SINGLE flat JSON object (do not use arrays). Summarize the overall status and total amounts where applicable.\n\n"
                + "If a field is not found or not applicable, use null.\n\nText: "
                + text;
    }
}
