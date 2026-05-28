package com.example.apibridge.adapter.resilience;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.exception.AIExtractionException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Wraps any AI provider call with a Retry + CircuitBreaker from Resilience4j.
 * <p>
 * Both patterns share the {@code ai-provider} instance name, configured in
 * {@code application.yml} under {@code resilience4j.retry} and
 * {@code resilience4j.circuitbreaker}.
 * </p>
 * <ul>
 *   <li>Retry — up to 3 attempts with 1 s back-off on transient failures.</li>
 *   <li>CircuitBreaker — opens after 50 % failure rate over 10 calls;
 *       waits 30 s before allowing probe requests through.</li>
 * </ul>
 */
@Component
public class AIProviderResilienceDecorator {

    private static final Logger log = LoggerFactory.getLogger(AIProviderResilienceDecorator.class);
    private static final String INSTANCE_NAME = "ai-provider";

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public AIProviderResilienceDecorator(RetryRegistry retryRegistry,
                                         CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retry = retryRegistry.retry(INSTANCE_NAME);
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(INSTANCE_NAME);

        // Log retry attempts so they are visible in the application log
        this.retry.getEventPublisher()
                .onRetry(e -> log.warn("AI provider call failed, retrying (attempt {}/{}): {}",
                        e.getNumberOfRetryAttempts(),
                        retry.getRetryConfig().getMaxAttempts(),
                        e.getLastThrowable().getMessage()));

        // Log circuit breaker state transitions
        this.circuitBreaker.getEventPublisher()
                .onStateTransition(e -> log.warn("AI provider circuit breaker state: {} → {}",
                        e.getStateTransition().getFromState(),
                        e.getStateTransition().getToState()));
    }

    /**
     * Executes {@code call} protected by retry then circuit breaker.
     * Any exception that exhausts retries or trips the breaker is wrapped
     * in {@link AIExtractionException}.
     */
    public AIResponse execute(Supplier<AIResponse> call) {
        Supplier<AIResponse> withRetry = Retry.decorateSupplier(retry, call);
        Supplier<AIResponse> withCircuitBreaker = CircuitBreaker.decorateSupplier(circuitBreaker, withRetry);
        try {
            return withCircuitBreaker.get();
        } catch (Exception e) {
            if (e instanceof AIExtractionException aee) throw aee;
            throw new AIExtractionException("AI provider unavailable: " + e.getMessage(), e);
        }
    }
}
