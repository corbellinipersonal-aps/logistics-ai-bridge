package com.example.apibridge.exception;

/**
 * Thrown when user-supplied input contains patterns indicative of a prompt injection attempt.
 */
public class PromptInjectionException extends RuntimeException {

    public PromptInjectionException(String message) {
        super(message);
    }
}
