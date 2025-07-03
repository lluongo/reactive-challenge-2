package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.application.port.PercentageService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationServiceImpl.class);
    
    private final PercentageService percentageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BigDecimal> calculateWithPercentage(BigDecimal num1, BigDecimal num2) {
        log.info("Calculating with numbers: {} and {}", num1, num2);
        
        // Validar inputs para evitar valores nulos o negativos
        if (num1 == null || num2 == null) {
            return Mono.error(new IllegalArgumentException("Input numbers cannot be null"));
        }
        
        BigDecimal sum = num1.add(num2);
        
        return percentageService.getPercentage()
                .map(percentage -> {
                    BigDecimal percentageAmount = sum.multiply(percentage);
                    BigDecimal result = sum.add(percentageAmount);
                    BigDecimal roundedResult = result.setScale(2, RoundingMode.HALF_UP);
                    
                    log.info("Calculation result: {} + {} = {}, applying percentage {}: final result = {}", 
                            num1, num2, sum, percentage, roundedResult);
                    
                    return roundedResult;
                })
                .doOnSuccess(result -> log.info("Calculation completed successfully with result: {}", result))
                .doOnError(error -> log.error("Error during calculation: {}", error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<CalculationResponse> processCalculationRequest(CalculationRequest request) {
        log.info("Processing calculation request: {}", request);
        
        return validateCalculationRequest(request)
                .flatMap(validRequest -> 
                    calculateWithPercentage(validRequest.getNum1(), validRequest.getNum2())
                        .map(result -> new CalculationResponse(result, validRequest.getNum1(), validRequest.getNum2()))
                )
                .doOnSuccess(response -> log.info("Calculation request processed successfully: {}", response))
                .doOnError(error -> log.error("Error processing calculation request: {}", error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<CalculationRequest> validateCalculationRequest(CalculationRequest request) {
        return Mono.just(request)
                .filter(req -> req.getNum1() != null && req.getNum2() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Both numbers must be provided")))
                .filter(req -> req.getNum1().compareTo(BigDecimal.ZERO) >= 0 && req.getNum2().compareTo(BigDecimal.ZERO) >= 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Numbers must be non-negative")))
                .doOnNext(validRequest -> log.debug("Request validated successfully: {}", validRequest))
                .doOnError(error -> log.warn("Request validation failed: {}", error.getMessage()));
    }
}
