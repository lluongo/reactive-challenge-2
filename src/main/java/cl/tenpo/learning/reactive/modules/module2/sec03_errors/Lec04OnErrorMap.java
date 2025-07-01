package cl.tenpo.learning.reactive.modules.module2.sec03_errors;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
public class Lec04OnErrorMap {

    public static void main(String[] args) {

        Mono.just("input")
                .flatMap(next -> someFunctionThatReturnsError())
                .doOnError(err -> log.error("Error class: {}", err.getClass().getSimpleName()))
                .onErrorMap(err -> new IOException("Encountered error running some function -> " + err.getMessage(), err))
                .doOnError(err -> log.error("Error class: {}", err.getClass().getSimpleName()))
                .subscribe(ModuleUtils.subscriber());

    }

    private static Mono<String> someFunctionThatReturnsError() {
        return Mono.error(() -> new RuntimeException("oops! server unavailable"));
    }

}
