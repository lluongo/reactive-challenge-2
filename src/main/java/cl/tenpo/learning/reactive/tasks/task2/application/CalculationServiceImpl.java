package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.application.port.PercentageService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private final PercentageService percentageService;

    @Override
    public Mono<CalculationResponse> processCalculationRequest(CalculationRequest request) {
        log.info("🛠️ Processing request: {}", request);

        return calculateWithPercentage(request.getNum1(), request.getNum2())
                .map(result -> {
                    log.info("🎯 Calculation successful: {}", result);
                    return CalculationResponse.builder()
                            .result(result)
                            .num1(request.getNum1())
                            .num2(request.getNum2())
                            .build();
                });
    }

    @Override
    public Mono<BigDecimal> calculateWithPercentage(BigDecimal num1, BigDecimal num2) {
        log.debug("🧮 Calculating with values: num1={}, num2={}", num1, num2);

        return Mono.just(num1.add(num2))
                .flatMap(sum -> percentageService.getPercentage()
                        .map(percentage -> applyPercentage(sum, percentage))
                )
                .doOnNext(result -> log.debug("📊 Final result after percentage: {}", result));
    }

    private BigDecimal applyPercentage(BigDecimal sum, BigDecimal percentage) {
        log.debug("📐 Applying percentage {} to sum {}", percentage, sum);
        return sum.add(sum.multiply(percentage)).setScale(2, RoundingMode.HALF_UP);
    }
}
