package cl.tenpo.learning.reactive.utils.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class CalculatorService {

    public Mono<BigDecimal> calculate(BigDecimal input) {
        return Mono.just(input)
                .map(num -> num.multiply(BigDecimal.TWO).add(BigDecimal.ONE).stripTrailingZeros());
    }

}
