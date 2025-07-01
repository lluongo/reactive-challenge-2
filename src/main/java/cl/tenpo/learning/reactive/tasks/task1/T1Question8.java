package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.exception.AuthorizationTimeoutException;
import cl.tenpo.learning.reactive.utils.exception.PaymentProcessingException;
import cl.tenpo.learning.reactive.utils.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class T1Question8 {

    private final TransactionService transactionService;

    public Mono<String> question8(int transactionId) {
        return transactionService
                .authorizeTransaction(transactionId)
                .timeout(Duration.ofSeconds(3),
                        Mono.error(new AuthorizationTimeoutException(
                                "[question8] Timeout al autorizar transacción " + transactionId
                        )))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(500))
                        .filter(ex -> !(ex instanceof AuthorizationTimeoutException))
                )
                .onErrorMap(err -> {
                    if (err instanceof AuthorizationTimeoutException) {
                        return err;
                    }
                    return new PaymentProcessingException(
                            "[question8] Error processing payment for transaction " + transactionId, err
                    );
                });
    }
}
