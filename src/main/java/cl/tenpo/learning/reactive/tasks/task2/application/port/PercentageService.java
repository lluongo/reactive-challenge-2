package cl.tenpo.learning.reactive.tasks.task2.application.port;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
public interface PercentageService {
    
    Mono<BigDecimal> getPercentage();
}
