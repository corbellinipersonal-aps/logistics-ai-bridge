package com.example.apibridge.controller;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionRequest;
import com.example.apibridge.port.NotificationPort;
import com.example.apibridge.service.AIService;
import com.example.apibridge.service.ExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SendController.class)
@ActiveProfiles("test")
public class SendControllerAIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @MockBean(name = "email")
    private NotificationPort emailNotifier;

    @MockBean(name = "slack")
    private NotificationPort slackNotifier;

    @MockBean
    private ExtractionService extractionService;

    @Autowired
    private ObjectMapper objectMapper;

    private AIResponse sampleAIResponse() {
        AIResponse r = new AIResponse();
        r.setCompanyName("ACME Corp");
        r.setDate("2023-01-01");
        r.setTotalAmount(123.45);
        return r;
    }

    @Test
    public void testSendAIExtractionToEmail() throws Exception {
        ExtractionRequest request = new ExtractionRequest();
        request.setText("Invoice from ACME Corp for $123.45 on 2023-01-01");
        when(aiService.extractData(any(ExtractionRequest.class))).thenReturn(sampleAIResponse());

        mockMvc.perform(post("/api/send/ai/email")
                .param("to", "test@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testSendAIExtractionToSlack() throws Exception {
        ExtractionRequest request = new ExtractionRequest();
        request.setText("Invoice from ACME Corp for $123.45 on 2023-01-01");
        when(aiService.extractData(any(ExtractionRequest.class))).thenReturn(sampleAIResponse());

        mockMvc.perform(post("/api/send/ai/slack")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
