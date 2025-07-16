package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Random;

@RestController
@RequestMapping("/external-api")
@Slf4j
public class ExternalPercentageController {

    private final Random random = new Random();

    @Value("${app.testing.force-external-api-error:false}")
    private boolean forceError;

    @GetMapping("/percentage")
    public Mono<BigDecimal> getPercentageRate() {
        log.info("External API percentage endpoint called - forceError={}", forceError);

        return Mono.just(forceError)
                .filter(shouldFail -> !shouldFail)
                .flatMap(ignored -> generatePercentage())
                .doOnSubscribe(s -> log.debug("Iniciando flujo de porcentaje"))
                .doOnSuccess(p -> log.info("Retornando porcentaje: {}", p))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Forzando error en API externa para pruebas de Kafka");
                    return Mono.error(new RuntimeException("Error forzado para prueba de Kafka"));
                }));
    }

    @GetMapping("/percentage/test")
    public Mono<BigDecimal> getPercentageRateTest(
            @RequestParam(defaultValue = "false") boolean forceErrorParam) {
        log.info("Test API percentage endpoint called - forceError={}", forceErrorParam);

        return Mono.just(forceErrorParam)
                .filter(shouldFail -> !shouldFail)
                .flatMap(ignored -> generatePercentage())
                .doOnNext(p -> log.info("Test retornando porcentaje: {}", p))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Forzando error en API externa para pruebas de Kafka");
                    return Mono.error(new RuntimeException("Error forzado para prueba de Kafka"));
                }));
    }

    private Mono<BigDecimal> generatePercentage() {
        return Mono.defer(() -> {
            int trigger = random.nextInt(10);
            Mono<Integer> valueMono = (trigger < 5)
                    ? Mono.delay(Duration.ofSeconds(10))
                    .thenReturn(random.nextInt(10) + 10)
                    : Mono.just(random.nextInt(10) + 10);

            return valueMono.map(n -> BigDecimal.valueOf(n).movePointLeft(2));
        });
    }
}
