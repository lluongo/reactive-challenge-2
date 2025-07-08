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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/external-api")
@Slf4j
public class ExternalPercentageController {

    private final Random random = new Random();
    
    @Value("${app.testing.force-external-api-error:false}")
    private boolean forceError;
    
    @GetMapping("/percentage")
    public Mono<Map<String, Object>> getPercentageRate() {
        log.info(" External API percentage endpoint called - forceError={}", forceError);
        
        return Mono.just(forceError)
                .filter(shouldFail -> !shouldFail)
                .flatMap(notUsed -> generatePercentage())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn(" Forzando error en API externa para pruebas de Kafka");
                    return Mono.error(new RuntimeException("Error forzado para prueba de Kafka"));
                }))
                .doOnSubscribe(s -> log.debug(" Iniciando flujo de porcentaje"))
                .doOnSuccess(r -> log.info(" Retornando porcentaje: {}", r))
                .doOnError(e -> log.error(" Error procesando porcentaje: {}", e.getMessage()));
    }
    
    private Mono<Map<String, Object>> generatePercentage() {
        return Mono.just(random)
                .map(r -> r.nextInt(10))
                .filter(number -> number < 5)
                .flatMap(number -> Mono.delay(Duration.ofSeconds(10)))
                .then(Mono.defer(this::buildPercentageRate));
    }
    
    /**
     * Endpoint para pruebas que permite controlar dinámicamente si se fuerza un error
     */
    @GetMapping("/percentage/test")
    public Mono<Map<String, Object>> getPercentageRateTest(@RequestParam(defaultValue = "false") boolean forceError) {
        log.info(" Test API percentage endpoint called - forceError={}", forceError);
        
        return Mono.just(forceError)
                .filter(shouldFail -> !shouldFail)
                .flatMap(notUsed -> buildPercentageRate())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn(" Forzando error en API externa para pruebas de Kafka");
                    return Mono.error(new RuntimeException("Error forzado para prueba de Kafka"));
                }))
                .doOnNext(r -> log.info(" Test retornando porcentaje: {}", r))
                .doOnError(e -> log.warn(" Error forzado para pruebas: {}", e.getMessage()));
    }
    
    private Mono<Map<String, Object>> buildPercentageRate() {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            // Generar un porcentaje aleatorio entre 0.1 y 0.2 (10% y 20%)
            BigDecimal percentage = new BigDecimal(String.format("0.%d", random.nextInt(10) + 10));
            result.put("percentage", percentage.toString());
            result.put("timestamp", LocalDateTime.now());
            return result;
        });
    }
}
