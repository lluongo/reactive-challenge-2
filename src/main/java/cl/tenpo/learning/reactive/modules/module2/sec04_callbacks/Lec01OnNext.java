package cl.tenpo.learning.reactive.modules.module2.sec04_callbacks;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Lec01OnNext {

    public static void main(String[] args) {

        Mono.just("Hello")
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .map(next -> next.concat(" World!"))
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

}
