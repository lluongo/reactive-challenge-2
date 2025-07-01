package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class Lec13Merge {

    public static void main(String[] args) {

        Mono<String> userName = Mono.just("Juan Pérez").delayElement(Duration.ofMillis(500));
        Mono<Integer> userAge = Mono.just(30).delayElement(Duration.ofMillis(300));
        Mono<String> userCity = Mono.just("Buenos Aires").delayElement(Duration.ofMillis(700));

        Flux<Object> mergedFlux = Flux.merge(userName, userAge, userCity);

        mergedFlux.subscribe(ModuleUtils.subscriber());
        ModuleUtils.sleepSeconds(5);

    }

}
