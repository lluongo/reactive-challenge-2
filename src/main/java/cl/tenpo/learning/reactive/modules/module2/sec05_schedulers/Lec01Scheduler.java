package cl.tenpo.learning.reactive.modules.module2.sec05_schedulers;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Lec01Scheduler {

    public static void main(String[] args) {

        Flux.range(1, 5)
                .doOnNext(i -> log.info("Generando número: {}", i))
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Multiplicando: {}", i);
                    return i * 10;
                })
                .publishOn(Schedulers.parallel())
                .map(i -> {
                    log.info("Operación pesada: {}", i);
                    return i + " procesado";
                })
                .subscribe(result -> log.info("Recibido: {}", result));

        ModuleUtils.sleepSeconds(10);

    }

}
