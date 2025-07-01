package cl.tenpo.learning.reactive.modules.module2.sec04_callbacks;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Lec01OnNextFlux {

    public static void main(String[] args) {

        Flux.just("Hello", "World", "!")
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

}
