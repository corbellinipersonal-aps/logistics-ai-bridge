package com.example.apibridge.controller;

import com.example.apibridge.demo.DemoPopulationResult;
import com.example.apibridge.demo.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo Operations", description = "Optional demo utilities for local showcase and seed data")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/health")
    @Operation(summary = "Demo profile health check", description = "Returns 200 when the demo profile is active.")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("demo profile active");
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset Database", description = "Deletes all extractions and ensures a clean state for the demo.")
    public ResponseEntity<String> resetDatabase() {
        demoService.resetDatabase();
        return ResponseEntity.ok("Database reset successful. Ready for demo!");
    }

    @PostMapping("/populate")
    @Operation(summary = "Populate Demo Data", description = "Reads sample texts from demo-assets and processes them through the AI pipeline.")
    public ResponseEntity<String> populateDemoData() {
        DemoPopulationResult result = demoService.populateFromAssets();
        if (result.getSuccessful() == 0) {
            return ResponseEntity.internalServerError().body(result.toSummaryMessage());
        }
        return ResponseEntity.ok(result.toSummaryMessage());
    }
}
