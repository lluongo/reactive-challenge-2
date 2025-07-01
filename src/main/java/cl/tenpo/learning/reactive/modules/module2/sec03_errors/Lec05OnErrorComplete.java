package cl.tenpo.learning.reactive.modules.module2.sec03_errors;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Lec05OnErrorComplete {

    public static void main(String[] args) {

        Mono.just("input")
                .flatMap(next -> someFunctionThatReturnsError())
                .doOnError(err -> log.error("Emitted onError: {}", err.getMessage()))
                .onErrorComplete()
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

    private static Mono<String> someFunctionThatReturnsError() {
        return Mono.error(() -> new RuntimeException("oops! server unavailable"));
    }

}
