package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

public class Lec07DefaultIfEmpty {

    public static void main(String[] args) {

        Flux<String> countriesFlux = Flux.empty();

        countriesFlux
                .defaultIfEmpty("Argentina")
                .subscribe(ModuleUtils.subscriber());

    }

}
