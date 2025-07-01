package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

public class Lec10Repeat {

    public static void main(String[] args) {

        Flux.just("Argentina", "Chile", "Perú", "Brasil", "Colombia")
                .repeat(3)
                .subscribe(ModuleUtils.subscriber());

    }

}
