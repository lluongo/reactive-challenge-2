package cl.tenpo.learning.reactive.modules.module2.sec04_callbacks;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Lec02OnErrorFlux {

    public static void main(String[] args) {

        Flux.just("Hello", "GoodBye", "Hello", "Hello", "Hello")
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .flatMap(Lec02OnErrorFlux::buildFinalMessage)
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .doOnError(err -> log.error("Emitted onError: {}", err.getMessage()))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

    private static Mono<String> buildFinalMessage(String input) {
        return Mono.just(input)
                .filter(next -> next.equals("Hello"))
                .map(next -> next.concat(" World!"))
                .switchIfEmpty(Mono.error(() -> new RuntimeException("Input can only be Hello. Input: " + input)));
    }

}
