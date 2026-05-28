package com.example.apibridge.adapter.notification;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.exception.NotificationException;
import com.example.apibridge.port.NotificationPort;
import com.example.apibridge.util.MessageFormatter;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Slack adapter for {@link NotificationPort}.
 * All Slack SDK / webhook details are confined here.
 */
@Component
@Qualifier("slack")
public class SlackNotificationAdapter implements NotificationPort {

    private final String webhookUrl;
    private final Slack slack;

    public SlackNotificationAdapter(@Value("${slack.webhook.url}") String webhookUrl, Slack slack) {
        this.webhookUrl = webhookUrl;
        this.slack = slack;
    }

    @Override
    public void sendExtraction(String recipient, ExtractionResponse extraction) {
        send(MessageFormatter.formatExtraction(extraction));
    }

    @Override
    public void sendAIExtraction(String recipient, AIResponse aiResponse) {
        send(MessageFormatter.formatAIExtraction(aiResponse));
    }

    private void send(String text) {
        try {
            slack.send(webhookUrl, Payload.builder().text(text).build());
        } catch (Exception e) {
            throw new NotificationException("Failed to send notification to Slack", e);
        }
    }
}
