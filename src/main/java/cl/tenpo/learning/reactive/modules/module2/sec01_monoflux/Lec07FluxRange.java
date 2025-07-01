package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

public class Lec07FluxRange {

    public static void main(String[] args) {

        Flux.range(10, 5)
                .subscribe(ModuleUtils.subscriber());

    }

}
