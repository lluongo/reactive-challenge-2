package cl.tenpo.learning.reactive.modules.module2.sec04_callbacks;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Lec03OnSuccessEmpty {

    public static void main(String[] args) {

        Mono.empty()
                .doOnNext(next -> log.info("onNext: {}", next))
                .doOnSuccess(next -> log.info("OnSuccess: {}", next))
                .subscribe(ModuleUtils.subscriber());

        ModuleUtils.sleepSeconds(5);

    }

}
