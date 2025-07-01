package cl.tenpo.learning.reactive.modules.module2.sec03_errors;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Lec01UnhandledError {

    public static void main(String[] args) {

        Mono.just("Hello")
                .flatMap(next -> someFunctionThatReturnsError())
                .doOnError(err -> log.error("Emitted onError: {}", err.getMessage()))
                .subscribe(
                        next -> log.info("Received onNext: {}", next)
                );

        ModuleUtils.sleepSeconds(5);

    }

    private static Mono<String> someFunctionThatReturnsError() {
        return Mono.error(() -> new RuntimeException("oops! server unavailable"));
    }

}
