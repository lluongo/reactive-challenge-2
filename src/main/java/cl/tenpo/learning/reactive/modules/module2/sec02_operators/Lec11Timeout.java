package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Lec11Timeout {

    public static void main(String[] args) {

        Flux.just("Argentina", "Chile", "Perú", "Brasil", "Colombia", "Venezuela", "Ecuador")
                .delayElements(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(25);

    }

}
