package com.example.apibridge.demo;

import com.example.apibridge.adapter.notification.EmailNotificationAdapter;
import com.example.apibridge.adapter.notification.SlackNotificationAdapter;
import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionRequest;
import com.example.apibridge.port.AIProvider;
import com.example.apibridge.repository.ExtractionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
public class DemoStabilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExtractionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailNotificationAdapter emailNotifier;

    @MockBean
    private SlackNotificationAdapter slackNotifier;

    @MockBean
    private AIProvider aiProvider;

    @BeforeEach
    public void setUp() {
        doNothing().when(emailNotifier).sendAIExtraction(any(), any());
        doNothing().when(slackNotifier).sendAIExtraction(any(), any());
    }

    @Test
    public void verifyDemoSequenceThreeTimes() throws Exception {
        for (int i = 1; i <= 3; i++) {
            System.out.println(">>> Starting Demo Run #" + i);

            // 1. Reset Database
            mockMvc.perform(post("/api/demo/reset"))
                    .andExpect(status().isOk());

            assertEquals(0, repository.count(), "Repository should be empty after reset in run " + i);

            // 2. Perform AI Extraction (Mocked AI Provider)
            AIResponse mockResponse = new AIResponse();
            mockResponse.setCompanyName("DemoCorp-" + i);
            mockResponse.setDate("2026-02-0" + i);
            mockResponse.setTotalAmount(100.0 * i);

            when(aiProvider.extract(anyString())).thenReturn(mockResponse);

            ExtractionRequest request = new ExtractionRequest();
            request.setText("Sample invoice for DemoCorp-" + i);

            mockMvc.perform(post("/api/send/ai/email")
                    .param("to", "demo@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 3. Verify data was saved
            assertEquals(1, repository.count(), "One extraction should be saved after step 2 in run " + i);

            System.out.println(">>> Demo Run #" + i + " completed successfully.");
        }
    }
}