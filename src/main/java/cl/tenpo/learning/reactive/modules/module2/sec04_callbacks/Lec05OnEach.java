package cl.tenpo.learning.reactive.modules.module2.sec04_callbacks;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

@Slf4j
public class Lec05OnEach {

    public static void main(String[] args) {

        Mono.just("Hello")
                .map(next -> next.concat(" World!"))
                .doOnEach(Lec05OnEach::logOnEach)
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

    private static void logOnEach(Signal<String> signal) {
        if (signal.isOnComplete()) {
            log.info("Signal value is null -> {}", signal.get());
            log.info("Emitted onComplete");
        } else if(signal.isOnError()) {
            log.error("Emitted onError {}", signal.getThrowable().getMessage());
        } else if(signal.isOnNext()) {
            log.info("Emitted onNext {}", signal.get());
        }
    }

}
