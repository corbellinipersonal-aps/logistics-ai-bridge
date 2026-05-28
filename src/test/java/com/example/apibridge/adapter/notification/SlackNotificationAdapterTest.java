package com.example.apibridge.adapter.notification;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.exception.NotificationException;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SlackNotificationAdapterTest {

    private SlackNotificationAdapter adapter;

    @Mock
    private Slack slack;

    private final String webhookUrl = "http://localhost/mock-webhook";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new SlackNotificationAdapter(webhookUrl, slack);
    }

    @Test
    public void testSendExtractionSuccess() throws IOException {
        ExtractionResponse extraction = new ExtractionResponse();
        extraction.setCompanyName("Test Corp");
        when(slack.send(eq(webhookUrl), any(Payload.class))).thenReturn(mock(WebhookResponse.class));

        adapter.sendExtraction(null, extraction);

        verify(slack, times(1)).send(eq(webhookUrl), any(Payload.class));
    }

    @Test
    public void testSendAIExtractionSuccess() throws IOException {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setCompanyName("AI Corp");
        when(slack.send(eq(webhookUrl), any(Payload.class))).thenReturn(mock(WebhookResponse.class));

        adapter.sendAIExtraction(null, aiResponse);

        verify(slack, times(1)).send(eq(webhookUrl), any(Payload.class));
    }

    @Test
    public void testSendFailureThrowsNotificationException() throws IOException {
        ExtractionResponse extraction = new ExtractionResponse();
        when(slack.send(anyString(), any(Payload.class))).thenThrow(new IOException("Connection failed"));

        assertThrows(NotificationException.class, () -> adapter.sendExtraction(null, extraction));
    }
}
