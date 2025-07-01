package cl.tenpo.learning.reactive.modules.module2.sec01_monoflux;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Mono;

public class Lec03MonoError {

    public static void main(String[] args) {

        Mono.error(new RuntimeException("error !!!!"))
                .subscribe(ModuleUtils.subscriber());

    }

}
