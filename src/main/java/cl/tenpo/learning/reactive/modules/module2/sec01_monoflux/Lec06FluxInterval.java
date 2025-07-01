package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Lec06FluxInterval {

    public static void main(String[] args) {

        Flux.interval(Duration.ofMillis(500))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(10);

    }

}
