package cl.tenpo.learning.reactive.modules.module2.sec03_errors;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Mono;

public class Lec03OnErrorResume {

    public static void main(String[] args) {

        Mono.just("Hello")
                .flatMap(next -> someFunctionThatReturnsError())
                .onErrorResume(err -> fallbackPublisher())
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

    private static Mono<String> someFunctionThatReturnsError() {
        return Mono.error(() -> new RuntimeException("oops! server unavailable"));
    }

    private static Mono<String> fallbackPublisher() {
        return Mono.just("Fallback using a Publisher");
    }

}
