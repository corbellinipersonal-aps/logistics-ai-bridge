package com.example.apibridge.demo;

import com.example.apibridge.service.ExtractionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DemoServiceTest {

    @Mock
    private ExtractionService extractionService;

    @Mock
    private com.example.apibridge.service.AIService aiService;

    @InjectMocks
    private DemoService demoService;

    @Test
    public void resetDatabaseDelegatesToExtractionService() {
        demoService.resetDatabase();
        verify(extractionService).clearAll();
    }
}
