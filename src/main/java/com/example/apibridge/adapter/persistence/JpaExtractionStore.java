package com.example.apibridge.adapter.persistence;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.exception.ResourceNotFoundException;
import com.example.apibridge.mapper.ExtractionMapper;
import com.example.apibridge.model.Extraction;
import com.example.apibridge.port.ExtractionStore;
import com.example.apibridge.repository.ExtractionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA adapter for {@link ExtractionStore}.
 * All Spring Data / Hibernate details are confined here.
 */
@Component
public class JpaExtractionStore implements ExtractionStore {

    private final ExtractionRepository repository;
    private final ExtractionMapper mapper;

    public JpaExtractionStore(ExtractionRepository repository, ExtractionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ExtractionResponse save(AIResponse aiResponse) {
        Extraction saved = repository.save(mapper.toEntity(aiResponse));
        return mapper.toDto(saved);
    }

    @Override
    public List<ExtractionResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExtractionResponse findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Extraction not found with ID: " + id));
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
