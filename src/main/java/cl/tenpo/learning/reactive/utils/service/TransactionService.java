package cl.tenpo.learning.reactive.utils.service;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TransactionService {

    public Mono<String> authorizeTransaction(Integer transactionId) {
        return Mono.fromCallable(() -> {
            Double delay = (Math.random() * 6) + 1;
            ModuleUtils.sleepSeconds(delay.intValue());

            if (Math.random() < 0.5) {
                throw new RuntimeException("Servicio de autorización falló!");
            }

            return "Transacción " + transactionId + " autorizada";
        });
    }
}
