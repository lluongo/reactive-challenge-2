package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

public class Lec04FluxJust {

    public static void main(String[] args) {

        Flux.just("Elemento 1", "Elemento 2", "Elemento 3")
                .subscribe(ModuleUtils.subscriber());

    }

}
