package com.example.apibridge.port;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;

import java.util.List;

/**
 * Port for persistence of extraction data.
 * Keeps the application core independent of JPA / any specific storage technology.
 */
public interface ExtractionStore {

    ExtractionResponse save(AIResponse aiResponse);

    List<ExtractionResponse> findAll();

    ExtractionResponse findById(Long id);

    void deleteAll();
}
