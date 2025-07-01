package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

public class Lec08SwitchIfEmpty {

    public static void main(String[] args) {

        Flux<String> apiCountriesFlux = Flux.empty();
        Flux<String> apiCountriesBackUp = Flux.just("Argentina", "Chile", "Perú", "Brasil", "Colombia");

        apiCountriesFlux
                .switchIfEmpty(apiCountriesBackUp)
                .subscribe(ModuleUtils.subscriber());

    }

}
