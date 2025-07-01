package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Mono;

public class Lec02MonoEmpty {

    public static void main(String[] args) {

        Mono.empty()
                .subscribe(ModuleUtils.subscriber());

    }

}
