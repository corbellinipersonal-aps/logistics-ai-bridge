package com.example.apibridge.port;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;

/**
 * Port for outbound notification channels (Email, Slack, etc.).
 * Keeps the application core independent of any specific messaging technology.
 */
public interface NotificationPort {

    /**
     * Send a stored extraction record. The {@code recipient} semantics are
     * channel-specific (e.g. an email address for SMTP, ignored for Slack webhooks).
     */
    void sendExtraction(String recipient, ExtractionResponse extraction);

    /**
     * Send a freshly extracted AI result directly, without a persisted record.
     */
    void sendAIExtraction(String recipient, AIResponse aiResponse);
}
