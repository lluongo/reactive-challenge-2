package cl.tenpo.learning.reactive.tasks.task2.application.port;

import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Servicio para realizar cálculos matemáticos con porcentajes dinámicos.
 */
public interface CalculationService {
    
    /**
     * Realiza un cálculo sumando dos números y aplicando un porcentaje dinámico.
     * 
     * @param num1 primer número para el cálculo
     * @param num2 segundo número para el cálculo
     * @return el resultado del cálculo como BigDecimal
     */
    Mono<BigDecimal> calculateWithPercentage(BigDecimal num1, BigDecimal num2);
    
    /**
     * Procesa una petición de cálculo completa con validación y creación de respuesta.
     * 
     * @param request la petición de cálculo
     * @return la respuesta completa del cálculo
     */
    Mono<CalculationResponse> processCalculationRequest(CalculationRequest request);
    
    /**
     * Valida una petición de cálculo.
     * 
     * @param request la petición a validar
     * @return la petición validada o error
     */
    Mono<CalculationRequest> validateCalculationRequest(CalculationRequest request);
}
