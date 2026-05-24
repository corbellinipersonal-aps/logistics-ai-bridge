package com.example.apibridge.service;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionRequest;
import com.example.apibridge.port.AIProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AIServiceIntegrationTest {

    @Autowired
    private AIService aiService;

    @MockBean
    private AIProvider aiProvider;

    @Test
    public void testExtractData() {
        AIResponse mockResponse = new AIResponse();
        mockResponse.setCompanyName("ACME Corp");
        mockResponse.setDate("2023-01-01");
        mockResponse.setTotalAmount(123.45);

        when(aiProvider.extract(anyString())).thenReturn(mockResponse);

        ExtractionRequest request = new ExtractionRequest();
        request.setText("Invoice from ACME Corp for $123.45 on 2023-01-01");

        AIResponse response = aiService.extractData(request);

        assertNotNull(response);
        assertEquals("ACME Corp", response.getCompanyName());
        assertEquals("2023-01-01", response.getDate());
        assertEquals(123.45, response.getTotalAmount());
    }
}
