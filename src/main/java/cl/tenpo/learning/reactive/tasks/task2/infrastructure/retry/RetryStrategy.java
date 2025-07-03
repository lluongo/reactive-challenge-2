package cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry;

import reactor.util.retry.Retry;

public interface RetryStrategy {
    
    <T> Retry getRetrySpec(Class<T> targetClass);
}
