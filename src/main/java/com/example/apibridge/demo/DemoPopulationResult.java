package com.example.apibridge.demo;

import java.util.ArrayList;
import java.util.List;

public class DemoPopulationResult {

    private final int successful;
    private final int total;
    private final List<String> failures;

    public DemoPopulationResult(int successful, int total, List<String> failures) {
        this.successful = successful;
        this.total = total;
        this.failures = failures;
    }

    public int getSuccessful() {
        return successful;
    }

    public int getTotal() {
        return total;
    }

    public List<String> getFailures() {
        return failures;
    }

    public boolean isFullySuccessful() {
        return successful == total && failures.isEmpty();
    }

    public String toSummaryMessage() {
        if (successful == 0) {
            String reason = failures.isEmpty() ? "Unknown error" : failures.get(0);
            return "No demo records were created. First error: " + reason;
        }

        String summary = "Demo populate finished. Created " + successful + " of " + total + " records.";
        if (!failures.isEmpty()) {
            int previewCount = Math.min(3, failures.size());
            summary += " Failures: " + String.join(" | ", failures.subList(0, previewCount));
        }
        return summary;
    }

    public static DemoPopulationResult emptyAssets(String reason) {
        return new DemoPopulationResult(0, 0, List.of(reason));
    }

    public static DemoPopulationResult fromCounts(int successful, int total, List<String> failures) {
        return new DemoPopulationResult(successful, total, new ArrayList<>(failures));
    }
}
