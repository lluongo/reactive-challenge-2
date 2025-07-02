package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.Calculation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationService {

    private final ExternalPercentageService externalPercentageService;

    /**
     * Realiza un cálculo sumando dos números y aplicando un porcentaje adicional
     * obtenido del servicio externo.
     * 
     * @param num1 primer número para el cálculo
     * @param num2 segundo número para el cálculo
     * @return Mono con el resultado del cálculo
     */
    public Mono<BigDecimal> calculateWithPercentage(BigDecimal num1, BigDecimal num2) {
        log.info("Calculating with numbers: {} and {}", num1, num2);
        
        // Validar inputs para evitar valores nulos o negativos
        if (num1 == null || num2 == null) {
            return Mono.error(new IllegalArgumentException("Input numbers cannot be null"));
        }
        
        return externalPercentageService.getPercentage()
                .map(percentage -> performCalculation(num1, num2, percentage))
                .doOnNext(result -> log.info("Calculation completed successfully with result: {}", result))
                .doOnError(error -> log.error("Error during calculation: {}", error.getMessage()))
                .checkpoint("calculation-service-result");
    }
    
    /**
     * Realiza el cálculo aplicando el porcentaje al resultado de la suma.
     */
    private BigDecimal performCalculation(BigDecimal num1, BigDecimal num2, BigDecimal percentage) {
        Calculation calculation = Calculation.of(num1, num2, percentage);
        BigDecimal result = calculation.getResult().setScale(2, RoundingMode.HALF_UP);
        
        log.info("Calculation result: {} + {} = {}, applying percentage {}: final result = {}", 
                num1, num2, num1.add(num2), percentage, result);
                
        return result;
    }
}
