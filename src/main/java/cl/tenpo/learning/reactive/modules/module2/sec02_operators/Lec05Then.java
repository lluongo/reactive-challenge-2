package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Lec05Then {

    public static void main(String[] args) {

        Flux.just("Argentina", "Chile", "Perú", "Brasil", "Colombia")
                .then(Mono.just("Paises procesados"))
                .subscribe(ModuleUtils.subscriber());

    }

}
