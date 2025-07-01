package cl.tenpo.learning.reactive.modules.module2.sec07_hotNcold;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Lec01ColdPublisher {

    public static void main(String[] args) {

        Flux<Integer> numbers = Flux.range(1, 10)
                .delayElements(Duration.ofSeconds(1));

        numbers.subscribe(ModuleUtils.subscriber("Gonza"));

        ModuleUtils.sleepSeconds(3);

        numbers.subscribe(ModuleUtils.subscriber("Nico"));

        ModuleUtils.sleepSeconds(15);

    }

}
