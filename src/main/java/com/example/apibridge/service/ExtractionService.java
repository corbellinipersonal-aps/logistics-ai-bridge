package com.example.apibridge.service;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.port.ExtractionStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for persistence and retrieval of extraction data.
 * Depends only on the {@link ExtractionStore} port — no JPA or framework types leak in.
 */
@Service
public class ExtractionService {

    private final ExtractionStore store;

    public ExtractionService(ExtractionStore store) {
        this.store = store;
    }

    public ExtractionResponse saveAIExtraction(AIResponse aiResponse) {
        return store.save(aiResponse);
    }

    public List<ExtractionResponse> fetchAllExtractions() {
        return store.findAll();
    }

    public ExtractionResponse fetchExtractionById(Long id) {
        return store.findById(id);
    }

    public void clearAll() {
        store.deleteAll();
    }
}
