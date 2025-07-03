package cl.tenpo.learning.reactive.tasks.task2.application.port;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Servicio para obtener el porcentaje a aplicar en los cálculos.
 */
public interface PercentageService {
    
    /**
     * Obtiene el porcentaje a aplicar en los cálculos.
     * 
     * @return Mono con el porcentaje como BigDecimal
     */
    Mono<BigDecimal> getPercentage();
}
