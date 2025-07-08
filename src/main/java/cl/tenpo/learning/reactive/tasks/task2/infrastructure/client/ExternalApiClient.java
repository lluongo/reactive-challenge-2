package cl.tenpo.learning.reactive.tasks.task2.infrastructure.client;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry.RetryStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExternalApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);
    
    private final WebClient webClient;
    private final TimeoutConfig timeoutConfig;
    private final RetryStrategy retryStrategy;
    
    @Value("${app.api.external.base-url}${app.api.external.percentage-path}")
    private String percentagePath;

    public Mono<BigDecimal> fetchPercentage() {
        log.info("Fetching percentage from external API: {}", percentagePath);
        
        return webClient.get()
                .uri(percentagePath)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(timeoutConfig.getExternalApi())
                .retryWhen(retryStrategy.getRetrySpec(BigDecimal.class))
                .mapNotNull(this::extractPercentageFromResponse)
                .doOnDiscard(Object.class, response -> 
                    log.warn(" Respuesta sin porcentaje válido: {}", response))
                .flatMap(this::parsePercentageReactively)
                .doOnSubscribe(s -> log.info(" Iniciando llamada API: {}", percentagePath))
                .doOnNext(value -> log.info(" Porcentaje obtenido: {}", value))
                .doOnError(err -> log.error(" Error API: {}", err.toString()));
    }

    private String extractPercentageFromResponse(Map<String, Object> response) {
        return Optional.ofNullable(response)
                .map(r -> (String) r.get("percentage"))
                .orElse(null);
    }
    
    private Mono<BigDecimal> parsePercentageReactively(String percentageStr) {
        return Mono.fromCallable(() -> new BigDecimal(percentageStr))
                .onErrorResume(NumberFormatException.class, error -> {
                    String normalizedStr = percentageStr.replace(',', '.');
                    log.info("Converting comma format to decimal point: {} -> {}", percentageStr, normalizedStr);
                    return Mono.fromCallable(() -> new BigDecimal(normalizedStr));
                });
    }
}
