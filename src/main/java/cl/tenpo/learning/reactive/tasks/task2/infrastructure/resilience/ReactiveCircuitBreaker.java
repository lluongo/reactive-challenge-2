package cl.tenpo.learning.reactive.tasks.task2.infrastructure.resilience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit Breaker reactivo simple para proteger servicios externos.
 * Implementa los estados: CLOSED, OPEN, HALF_OPEN.
 */
@Slf4j
@Component
public class ReactiveCircuitBreaker {

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    
    // Configuración
    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration OPEN_STATE_DURATION = Duration.ofSeconds(30);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    public enum State {
        CLOSED,    // Operación normal
        OPEN,      // Circuit abierto - falla rápido
        HALF_OPEN  // Probando si el servicio se recuperó
    }

    /**
     * Ejecuta una operación protegida por el circuit breaker.
     * 
     * @param operation Operación a ejecutar
     * @param fallback Fallback en caso de falla
     * @return Mono con el resultado o fallback
     */
    public <T> Mono<T> executeWithFallback(Supplier<Mono<T>> operation, Supplier<T> fallback) {
        return Mono.defer(() -> {
            State currentState = getCurrentState();
            
            switch (currentState) {
                case OPEN:
                    log.warn("Circuit breaker is OPEN - using fallback");
                    return Mono.just(fallback.get());
                    
                case HALF_OPEN:
                    log.info("Circuit breaker is HALF_OPEN - attempting operation");
                    return executeOperation(operation, fallback, true);
                    
                case CLOSED:
                default:
                    return executeOperation(operation, fallback, false);
            }
        });
    }

    /**
     * Ejecuta la operación con manejo de éxito/falla.
     */
    private <T> Mono<T> executeOperation(Supplier<Mono<T>> operation, Supplier<T> fallback, boolean isHalfOpen) {
        return operation.get()
                .timeout(TIMEOUT)
                .doOnSuccess(result -> onSuccess(isHalfOpen))
                .doOnError(error -> onFailure(error))
                .onErrorReturn(fallback.get());
    }

    /**
     * Maneja el éxito de una operación.
     */
    private void onSuccess(boolean wasHalfOpen) {
        if (wasHalfOpen) {
            log.info("Circuit breaker: Operation succeeded in HALF_OPEN, closing circuit");
            state.set(State.CLOSED);
        }
        failureCount.set(0);
    }

    /**
     * Maneja la falla de una operación.
     */
    private void onFailure(Throwable error) {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(Instant.now());
        
        log.warn("Circuit breaker: Operation failed (failure count: {}): {}", failures, error.getMessage());
        
        if (failures >= FAILURE_THRESHOLD) {
            state.set(State.OPEN);
            log.error("Circuit breaker opened due to {} failures", failures);
        }
    }

    /**
     * Determina el estado actual del circuit breaker.
     */
    private State getCurrentState() {
        State currentState = state.get();
        
        if (currentState == State.OPEN) {
            Instant lastFailure = lastFailureTime.get();
            if (lastFailure != null && 
                Duration.between(lastFailure, Instant.now()).compareTo(OPEN_STATE_DURATION) > 0) {
                
                log.info("Circuit breaker: Transitioning from OPEN to HALF_OPEN");
                state.compareAndSet(State.OPEN, State.HALF_OPEN);
                return State.HALF_OPEN;
            }
        }
        
        return currentState;
    }

    /**
     * Obtiene el estado actual para monitoreo.
     */
    public State getState() {
        return getCurrentState();
    }

    /**
     * Obtiene el número de fallas actuales.
     */
    public int getFailureCount() {
        return failureCount.get();
    }
}
