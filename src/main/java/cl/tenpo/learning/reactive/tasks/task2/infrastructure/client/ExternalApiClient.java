package cl.tenpo.learning.reactive.tasks.task2.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExternalApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);
    
    private final WebClient webClient;
    
    @Value("${app.api.external.base-url}${app.api.external.percentage-path}")
    private String percentagePath;

    public Mono<BigDecimal> fetchPercentage() {
        log.info("Fetching percentage from external API: {}", percentagePath);
        
        return webClient.get()
                .uri(percentagePath)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(this::extractPercentageFromResponseReactively)
                .doOnSubscribe(s -> log.error("🌐🌐🌐 CALLING EXTERNAL API: {} 🌐🌐🌐", percentagePath))
                .doOnError(err -> log.error("🌐🌐🌐 EXTERNAL API ERROR: {} 🌐🌐🌐", 
                        err != null ? err.toString() : "Unknown error"));
    }

    private Mono<BigDecimal> extractPercentageFromResponseReactively(Map<String, Object> response) {
        String percentageStr = (String) response.get("percentage");
        log.info("Received percentage from external API: {}", percentageStr);
        
        return parsePercentageReactively(percentageStr);
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
