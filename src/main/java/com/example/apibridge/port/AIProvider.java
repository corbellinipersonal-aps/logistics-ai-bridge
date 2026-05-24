package com.example.apibridge.port;

import com.example.apibridge.dto.AIResponse;

/**
 * Port for external LLM extraction adapters (Groq, OpenAI, Gemini, etc.).
 * Keeps the application core independent of any single provider implementation.
 */
public interface AIProvider {

    AIResponse extract(String prompt);
}
