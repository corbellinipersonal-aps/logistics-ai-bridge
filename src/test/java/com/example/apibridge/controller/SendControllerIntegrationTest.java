package com.example.apibridge.controller;

import com.example.apibridge.adapter.notification.EmailNotificationAdapter;
import com.example.apibridge.adapter.notification.SlackNotificationAdapter;
import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.model.Extraction;
import com.example.apibridge.port.NotificationPort;
import com.example.apibridge.repository.ExtractionRepository;
import com.example.apibridge.util.MessageFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SendControllerIntegrationTest {

    // Replace real notification adapters with no-op stubs for integration tests
    @org.springframework.boot.test.context.TestConfiguration
    static class MockNotificationConfig {

        @Bean
        @Qualifier("email")
        public NotificationPort emailNotifier() {
            return new NotificationPort() {
                @Override
                public void sendExtraction(String recipient, ExtractionResponse extraction) {
                    System.out.printf("[MOCK EMAIL] To: %s%n%s%n", recipient, MessageFormatter.formatExtraction(extraction));
                }
                @Override
                public void sendAIExtraction(String recipient, AIResponse aiResponse) {
                    System.out.printf("[MOCK EMAIL] To: %s%n%s%n", recipient, MessageFormatter.formatAIExtraction(aiResponse));
                }
            };
        }

        @Bean
        @Qualifier("slack")
        public NotificationPort slackNotifier() {
            return new NotificationPort() {
                @Override
                public void sendExtraction(String recipient, ExtractionResponse extraction) {
                    System.out.printf("[MOCK SLACK] %s%n", MessageFormatter.formatExtraction(extraction));
                }
                @Override
                public void sendAIExtraction(String recipient, AIResponse aiResponse) {
                    System.out.printf("[MOCK SLACK] %s%n", MessageFormatter.formatAIExtraction(aiResponse));
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExtractionRepository extractionRepository;

    private Long extractionId;

    @BeforeEach
    void setUp() {
        extractionRepository.deleteAll();
        Extraction extraction = new Extraction();
        extraction.setCompanyName("TestCorp");
        extraction.setDate("2024-01-01");
        extraction.setTotalAmount(1000.0);
        extractionId = extractionRepository.save(extraction).getId();
    }

    @Test
    void sendToEmail_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/send/email/" + extractionId)
                .param("to", "test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void sendToSlack_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/send/slack/" + extractionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
