package cl.tenpo.learning.reactive.tasks.task2.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * Cliente para comunicación con la API externa que provee el porcentaje.
 */
@Component
@RequiredArgsConstructor
public class ExternalApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);
    
    private final WebClient webClient;
    
    @Value("${app.api.external.base-url}${app.api.external.percentage-path}")
    private String percentagePath;

    /**
     * Obtiene el porcentaje desde la API externa.
     * 
     * @return Mono con el porcentaje como BigDecimal
     */
    public Mono<BigDecimal> fetchPercentage() {
        log.info("Fetching percentage from external API: {}", percentagePath);
        return webClient.get()
                .uri(percentagePath)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractPercentageFromResponse);
    }
    
    /**
     * Extrae el porcentaje de la respuesta del API.
     * Maneja tanto formato con punto decimal como con coma decimal.
     */
    private BigDecimal extractPercentageFromResponse(Map<String, Object> response) {
        String percentageStr = (String) response.get("percentage");
        log.info("Received percentage from external API: {}", percentageStr);
        
        // Manejar tanto formato americano (punto) como europeo (coma)
        try {
            // Intentar parsear directamente (formato con punto)
            return new BigDecimal(percentageStr);
        } catch (NumberFormatException e) {
            // Si falla, intentar reemplazar coma por punto
            String normalizedStr = percentageStr.replace(',', '.');
            log.info("Converting comma format to decimal point: {} -> {}", percentageStr, normalizedStr);
            return new BigDecimal(normalizedStr);
        }
    }
}
