package cl.tenpo.learning.reactive.tasks.task1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question6 {

    public ConnectableFlux<Double> question6() {
        Flux<Long> ticker = Flux.interval(Duration.ofMillis(500))
                .doOnSubscribe(sub -> log.info("[question6] ticker subscription started"))
                .doOnError(err -> log.error("[question6] ticker error", err))
                .doOnCancel(() -> log.info("[question6] ticker cancelled"));

        Flux<Double> priceFlux = ticker
                .map(__ -> ThreadLocalRandom.current().nextDouble(1.0, 501.0))
                .doOnSubscribe(sub -> log.info("[question6] priceFlux subscription started"))
                .doOnNext(price -> log.info("[question6] generated price={}", price))
                .doOnError(err -> log.error("[question6] priceFlux error", err))
                .doOnComplete(() -> log.info("[question6] priceFlux completed"));

        ConnectableFlux<Double> hotPrices = priceFlux.publish();

        hotPrices.connect();

        return hotPrices;
    }

}