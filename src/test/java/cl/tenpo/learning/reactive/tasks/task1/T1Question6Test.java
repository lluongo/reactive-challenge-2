package cl.tenpo.learning.reactive.tasks.task1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.ConnectableFlux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class T1Question6Test {

    @InjectMocks
    private T1Question6 t1Question6;

    @Test
    @DisplayName("PREGUNTA 6 - Stock Prices")
    public void question6_uc1_test() {

            ConnectableFlux<Double> stockPrices = t1Question6.question6();

            stockPrices.connect();

            List<Double> firstSubscriberPrices = new ArrayList<>();
            stockPrices.subscribe(firstSubscriberPrices::add);

            StepVerifier.create(stockPrices.delaySubscription(Duration.ofSeconds(2)))
                    .expectSubscription()
                    .thenAwait(Duration.ofMillis(500))
                    .expectNextMatches(price -> price >= 1 && price <= 500)
                    .expectNextCount(2)
                    .thenCancel()
                    .verify();

            assertThat(firstSubscriberPrices).hasSizeGreaterThan(3);
        }

    }
