package cl.tenpo.learning.reactive.modules.module2.sec03_errors;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
public class Lec06CombiningErrorHandlers {

    public static void main(String[] args) {

        Mono.just("input")
                .flatMap(next -> someFunctionThatReturnsError())
                .doOnError(err -> log.error("Emitted onError: {}", err.getMessage()))
                .onErrorResume(IllegalArgumentException.class, err -> fallbackValue())
                .onErrorMap(IOException.class, err -> new RuntimeException("Error on some function " + err.getMessage(), err))
                .doOnNext(next -> log.info("Emitted onNext: {}", next))
                .doOnError(err -> log.error("Emitted onError: {}", err.getMessage()))
                .subscribe(ModuleUtils.subscriber());

    }

    private static Mono<Object> someFunctionThatReturnsError() {
        return Mono.just(Math.random())
                .filter(n -> n > 0.5)
                .flatMap(n -> Mono.error(() -> new IOException("Some IO exception encountered")))
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Some IllegalArgument exception encountered")));
    }

    private static Mono<String> fallbackValue() {
        return Mono.just("Fallback output");
    }

}
