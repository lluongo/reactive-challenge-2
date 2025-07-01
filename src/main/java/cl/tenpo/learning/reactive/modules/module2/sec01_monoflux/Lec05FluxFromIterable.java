package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

import java.util.List;

public class Lec05FluxFromIterable {

    public static void main(String[] args) {

        Flux.fromIterable(List.of(1, 2, 3, 4))
                .subscribe(ModuleUtils.subscriber());

    }

}
