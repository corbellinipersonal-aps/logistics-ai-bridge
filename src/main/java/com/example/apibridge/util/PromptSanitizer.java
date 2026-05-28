package com.example.apibridge.util;

import com.example.apibridge.exception.PromptInjectionException;

import java.util.List;
import java.util.Locale;

/**
 * Defends against prompt injection attacks before user text reaches the LLM.
 *
 * <p>Two complementary layers:</p>
 * <ol>
 *   <li><b>Heuristic keyword filter</b> — rejects input that contains phrases
 *       commonly used in injection attempts (e.g. "ignore all previous instructions").
 *       Applied first so obviously malicious payloads never reach the model.</li>
 *   <li><b>Structural delimiter wrapping</b> — wraps the sanitized text in
 *       {@code <user_input>} XML tags. The system prompt instructs the model that
 *       nothing inside those tags can override extraction rules, making it harder
 *       for subtle injections to succeed even if they slip past the keyword filter.</li>
 * </ol>
 */
public final class PromptSanitizer {

    private PromptSanitizer() {}

    /**
     * Phrases that are strong signals of a prompt injection attempt.
     * Checked case-insensitively against the raw input.
     */
    private static final List<String> INJECTION_PATTERNS = List.of(
            "ignore all previous instructions",
            "ignore previous instructions",
            "disregard previous instructions",
            "forget your instructions",
            "forget your rules",
            "system override",
            "override instructions",
            "you are now",
            "act as if",
            "new instructions:",
            "your new role",
            "pretend you are",
            "do not follow",
            "bypass your",
            "jailbreak"
    );

    /**
     * Validates and wraps {@code userText} for safe inclusion in an LLM prompt.
     *
     * @param userText raw text supplied by the caller
     * @return the text wrapped in {@code <user_input>} delimiters
     * @throws PromptInjectionException if a known injection pattern is detected
     * @throws IllegalArgumentException if {@code userText} is null or blank
     */
    public static String sanitize(String userText) {
        if (userText == null || userText.isBlank()) {
            throw new IllegalArgumentException("Input text cannot be blank.");
        }

        String lower = userText.toLowerCase(Locale.ROOT);
        for (String pattern : INJECTION_PATTERNS) {
            if (lower.contains(pattern)) {
                throw new PromptInjectionException(
                        "Input rejected: potential prompt injection detected.");
            }
        }

        // Wrap in structural delimiters so the model treats this block as data, not instructions
        return "<user_input>\n" + userText + "\n</user_input>";
    }
}
