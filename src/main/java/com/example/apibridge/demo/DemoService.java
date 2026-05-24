package com.example.apibridge.demo;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionRequest;
import com.example.apibridge.service.AIService;
import com.example.apibridge.service.ExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Demo-only application service for seeding and resetting showcase data.
 * Isolated from the core extraction pipeline and only active under the {@code demo} profile.
 */
@Service
@Profile("demo")
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final AIService aiService;
    private final ExtractionService extractionService;

    public DemoService(AIService aiService, ExtractionService extractionService) {
        this.aiService = aiService;
        this.extractionService = extractionService;
    }

    public void resetDatabase() {
        extractionService.clearAll();
    }

    public DemoPopulationResult populateFromAssets() {
        Path demoAssetsDir = Paths.get("demo-assets");
        if (!Files.exists(demoAssetsDir)) {
            return DemoPopulationResult.emptyAssets("demo-assets directory not found");
        }

        List<Path> demoFiles;
        try (Stream<Path> paths = Files.list(demoAssetsDir)) {
            demoFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return DemoPopulationResult.emptyAssets("Failed to list demo assets: " + e.getMessage());
        }

        if (demoFiles.isEmpty()) {
            return DemoPopulationResult.emptyAssets("No demo .txt files found in demo-assets");
        }

        int successful = 0;
        List<String> failures = new ArrayList<>();

        for (Path path : demoFiles) {
            try {
                String content = Files.readString(path);
                log.info("Processing demo file: {}", path.getFileName());
                ExtractionRequest request = new ExtractionRequest();
                request.setText(content);
                AIResponse aiResponse = aiService.extractData(request);
                extractionService.saveAIExtraction(aiResponse);
                successful++;
            } catch (Exception e) {
                log.error("Failed to process demo file {}: {}", path, e.getMessage());
                failures.add(path.getFileName() + ": " + e.getMessage());
            }
        }

        return DemoPopulationResult.fromCounts(successful, demoFiles.size(), failures);
    }
}
