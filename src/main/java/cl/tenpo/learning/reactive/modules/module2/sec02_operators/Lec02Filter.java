package cl.tenpo.learning.reactive.modules.module2.sec02_operators;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import reactor.core.publisher.Flux;

public class Lec02Filter {

    public static void main(String[] args) {

        Flux.<String>generate(synchronousSink ->
                        synchronousSink.next(ModuleUtils.faker().country().name()))
                .filter(s -> s.startsWith("A"))
                .take(10)
                .subscribe(ModuleUtils.subscriber());

    }

}
