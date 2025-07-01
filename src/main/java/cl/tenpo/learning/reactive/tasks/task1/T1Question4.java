package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question4 {

    private final CountryService countryService;

    public Flux<String> question4A() {

        Flux<String> batch = countryService
                .findAllCountries()
                .take(200)
                .cache();

        Mono<Map<String, Long>> countsMono = batch
                .collectList()
                .map(list -> list.stream()
                        .collect(Collectors.groupingBy(
                                country -> country,
                                Collectors.counting()
                        ))
                );

        countsMono.subscribe(
                countsMap -> log.info("[question4A] country counts: {}", countsMap),
                error -> log.error("[question4A] error while counting countries", error)
        );

        return batch
                .distinct()
                .sort()
                .doOnNext(country -> log.info("[question4A] emitting country='{}'", country));
    }
}
