package cl.tenpo.learning.reactive.tasks.task2.application.port;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Servicio para realizar cálculos con porcentajes.
 */
public interface CalculationService {
    
    /**
     * Realiza un cálculo sumando dos números y aplicando un porcentaje adicional
     * obtenido del servicio externo.
     * 
     * @param num1 primer número para el cálculo
     * @param num2 segundo número para el cálculo
     * @return Mono con el resultado del cálculo
     */
    Mono<BigDecimal> calculateWithPercentage(BigDecimal num1, BigDecimal num2);
}
