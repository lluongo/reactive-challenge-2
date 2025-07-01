package cl.tenpo.learning.reactive.modules.module2.sec04_callbacks;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Lec02OnError {

    public static void main(String[] args) {

        Mono.just("GoodBye")
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .flatMap(Lec02OnError::buildFinalMessage)
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .doOnError(err -> log.error("Emitted onError: {}", err.getMessage()))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

    private static Mono<String> buildFinalMessage(String input) {
        return Mono.just(input)
                .filter(next -> next.equals("Hello"))
                .switchIfEmpty(Mono.error(() -> new RuntimeException("Input can only be Hello. Input: " + input)));
    }

}
