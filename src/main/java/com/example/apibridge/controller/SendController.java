package com.example.apibridge.controller;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionRequest;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.port.NotificationPort;
import com.example.apibridge.service.AIService;
import com.example.apibridge.service.ExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/send")
@Tag(name = "Sending Operations", description = "Endpoints for triggering email and Slack notifications")
public class SendController {

    private final ExtractionService extractionService;
    private final AIService aiService;
    private final NotificationPort emailNotifier;
    private final NotificationPort slackNotifier;

    public SendController(ExtractionService extractionService,
                          AIService aiService,
                          @Qualifier("email") NotificationPort emailNotifier,
                          @Qualifier("slack") NotificationPort slackNotifier) {
        this.extractionService = extractionService;
        this.aiService = aiService;
        this.emailNotifier = emailNotifier;
        this.slackNotifier = slackNotifier;
    }

    @PostMapping("/email/{id}")
    @Operation(summary = "Send existing extraction to email",
               description = "Sends a pre-existing extraction record (by ID) to the specified email address.")
    @ApiResponse(responseCode = "200", description = "Email sent successfully")
    @ApiResponse(responseCode = "404", description = "Extraction not found")
    public ResponseEntity<String> sendToEmail(@PathVariable Long id, @RequestParam String to) {
        ExtractionResponse extraction = extractionService.fetchExtractionById(id);
        emailNotifier.sendExtraction(to, extraction);
        return ResponseEntity.ok("Sent to email");
    }

    @PostMapping("/slack/{id}")
    @Operation(summary = "Send existing extraction to Slack",
               description = "Sends a pre-existing extraction record (by ID) to the configured Slack channel.")
    @ApiResponse(responseCode = "200", description = "Message sent to Slack successfully")
    @ApiResponse(responseCode = "404", description = "Extraction not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<String> sendToSlack(@PathVariable Long id) {
        ExtractionResponse extraction = extractionService.fetchExtractionById(id);
        slackNotifier.sendExtraction(null, extraction);
        return ResponseEntity.ok("Sent to Slack");
    }

    @PostMapping("/ai/email")
    @Operation(summary = "Extract and send to email",
               description = "Extracts data from raw text using AI and sends it to an email.")
    @ApiResponse(responseCode = "200", description = "Extraction sent to email")
    @ApiResponse(responseCode = "400", description = "Failed to extract data")
    public ResponseEntity<String> sendAIExtractionToEmail(@Valid @RequestBody ExtractionRequest request,
                                                          @RequestParam String to) {
        AIResponse aiResponse = aiService.extractData(request);
        if (aiResponse == null)
            return ResponseEntity.badRequest().body("Failed to extract data from text.");
        extractionService.saveAIExtraction(aiResponse);
        emailNotifier.sendAIExtraction(to, aiResponse);
        return ResponseEntity.ok("Sent AI extraction to email");
    }

    @PostMapping("/ai/slack")
    @Operation(summary = "Extract and send to Slack",
               description = "Extracts data from raw text using AI and sends it to Slack.")
    @ApiResponse(responseCode = "200", description = "Extraction sent to Slack")
    @ApiResponse(responseCode = "400", description = "Failed to extract data")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<String> sendAIExtractionToSlack(@Valid @RequestBody ExtractionRequest request) {
        AIResponse aiResponse = aiService.extractData(request);
        if (aiResponse == null)
            return ResponseEntity.badRequest().body("Failed to extract data from text.");
        extractionService.saveAIExtraction(aiResponse);
        slackNotifier.sendAIExtraction(null, aiResponse);
        return ResponseEntity.ok("Sent AI extraction to Slack");
    }

    @PostMapping("/ai/extract")
    @Operation(summary = "Extract data using AI",
               description = "Returns the structured JSON data extracted from raw text without sending it anywhere.")
    @ApiResponse(responseCode = "200", description = "Data extracted successfully")
    @ApiResponse(responseCode = "400", description = "Failed to extract data")
    public ResponseEntity<AIResponse> extractData(@Valid @RequestBody ExtractionRequest request) {
        AIResponse aiResponse = aiService.extractData(request);
        if (aiResponse == null)
            return ResponseEntity.badRequest().build();
        extractionService.saveAIExtraction(aiResponse);
        return ResponseEntity.ok(aiResponse);
    }
}
