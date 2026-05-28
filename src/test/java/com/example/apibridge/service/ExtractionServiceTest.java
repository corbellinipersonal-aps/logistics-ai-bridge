package com.example.apibridge.service;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.exception.ResourceNotFoundException;
import com.example.apibridge.port.ExtractionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExtractionServiceTest {

    @Mock
    private ExtractionStore store;

    @InjectMocks
    private ExtractionService extractionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveAIExtraction() {
        AIResponse aiResponse = new AIResponse();
        ExtractionResponse expected = new ExtractionResponse();
        when(store.save(aiResponse)).thenReturn(expected);

        ExtractionResponse actual = extractionService.saveAIExtraction(aiResponse);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(store, times(1)).save(aiResponse);
    }

    @Test
    public void testFetchAllExtractions() {
        when(store.findAll()).thenReturn(Arrays.asList(new ExtractionResponse(), new ExtractionResponse()));

        List<ExtractionResponse> results = extractionService.fetchAllExtractions();

        assertEquals(2, results.size());
        verify(store, times(1)).findAll();
    }

    @Test
    public void testFetchExtractionByIdFound() {
        Long id = 1L;
        ExtractionResponse expected = new ExtractionResponse();
        when(store.findById(id)).thenReturn(expected);

        ExtractionResponse actual = extractionService.fetchExtractionById(id);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testFetchExtractionByIdNotFoundThrowsException() {
        Long id = 99L;
        when(store.findById(id)).thenThrow(new ResourceNotFoundException("Extraction not found with ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> extractionService.fetchExtractionById(id));
    }
}
