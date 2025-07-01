package cl.tenpo.learning.reactive.modules.module2.sec06_backpressure;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class Lec01BackPressure {

    public static void main(String[] args) {

        var producer = Flux.create(sink -> {
                    for (int i = 1; i <= 500 && !sink.isCancelled(); i++) {
                        log.info("generating {}", i);
                        sink.next(i);
                        ModuleUtils.sleep(Duration.ofMillis(50));
                    }
                    sink.complete();
                })
                .cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
                // .onBackpressureBuffer()
                // .onBackpressureError()
                // .onBackpressureBuffer(10)
                // .onBackpressureDrop()
                // .onBackpressureLatest()
                .log()
                .limitRate(1)
                .publishOn(Schedulers.boundedElastic())
                .map(Lec01BackPressure::timeConsumingTask)
                .doOnDiscard(Object.class, discarded -> log.info("Discarded elem: {}", discarded))
                .subscribe();

        ModuleUtils.sleepSeconds(60);
    }

    private static int timeConsumingTask(int i) {
        log.info("received: {}", i);
        ModuleUtils.sleepSeconds(1);
        return i;
    }

}
