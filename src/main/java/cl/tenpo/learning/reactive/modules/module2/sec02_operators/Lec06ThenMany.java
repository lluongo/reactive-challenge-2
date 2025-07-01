package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Lec06ThenMany {

    public static void main(String[] args) {

        Mono.just("Argentina")
                .thenMany(Flux.just("Chile", "Peru", "Brasil", "Colombia"))
                .subscribe(ModuleUtils.subscriber());

    }

}
