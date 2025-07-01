package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.service.CalculatorService;
import cl.tenpo.learning.reactive.utils.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question5 {

    private final CalculatorService calculatorService;
    private final UserService userService;

    public Mono<String> question5A() {
        return Flux
                .range(100, 901)
                .map(BigDecimal::valueOf)
                .concatMap(bd ->
                        calculatorService
                                .calculate(bd)
                                .defaultIfEmpty(BigDecimal.ZERO)
                                .onErrorMap(err -> err)
                )
                .collectList()
                .flatMap(results -> {
                    log.info("[question5A] all calculations succeeded (or were empty): {}", results);
                    return userService
                            .findFirstName()
                            .doOnSuccess(name -> log.info("[question5A] returning random name='{}'", name));
                })
                .onErrorResume(err -> {
                    log.warn("[question5A] error during calculations, returning 'Chuck Norris' – {}", err.getMessage());
                    return Mono.just("Chuck Norris");
                });
    }

    public Flux<String> question5B() {
        return Flux
                .range(100, 901)
                .map(BigDecimal::valueOf)
                .concatMap(bd ->
                        calculatorService
                                .calculate(bd)
                                .map(result -> Boolean.TRUE)
                                .defaultIfEmpty(Boolean.TRUE)
                                .onErrorMap(err -> err)
                )
                .collectList()
                .flatMapMany(
                        okList -> {
                            Flux<String> namesFlux = userService.findAllNames();
                            if (namesFlux == null) {
                                log.warn("[question5B] findAllNames() returned null, completing without names");
                                return Flux.empty();
                            }
                            return namesFlux
                                    .take(3)
                                    .doOnSubscribe(sub -> log.info("[question5B] emitting first 3 names"))
                                    .doOnNext(name -> log.info("[question5B] emitted name='{}'", name));
                        }
                )
                .onErrorResume(err -> {
                    log.warn("[question5B] error detected, completing without names – {}", err.getMessage());
                    return Flux.empty();
                });
    }
}
