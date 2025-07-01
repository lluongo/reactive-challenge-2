package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Mono;

public class Lec01MonoJust {

    public static void main(String[] args) {

        Mono.just("Hola, Reactive!")
                .subscribe(ModuleUtils.subscriber());

    }

}
