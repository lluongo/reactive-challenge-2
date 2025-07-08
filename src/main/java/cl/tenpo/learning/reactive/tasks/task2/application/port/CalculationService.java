package cl.tenpo.learning.reactive.tasks.task2.application.port;

import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface CalculationService {
    
    Mono<BigDecimal> calculateWithPercentage(BigDecimal num1, BigDecimal num2);
    
    Mono<CalculationResponse> processCalculationRequest(CalculationRequest request);

}
