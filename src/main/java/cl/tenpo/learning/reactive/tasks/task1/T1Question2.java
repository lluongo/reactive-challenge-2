package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question2 {

    private final CountryService countryService;

    public Flux<String> question2A() {
        return countryService
                .findAllCountries()
                .doOnSubscribe(sub -> log.info("[question2A] subscription started to findAllCountries()"))
                .distinct()
                .doOnNext(country -> log.info("[question2A] distinct country='{}'", country))
                .take(5)
                .doOnComplete(() -> log.info("[question2A] completed after collecting 5 distinct countries"))
                .doOnError(error -> log.error("[question2A] an error occurred", error));
    }

    public Flux<String> question2B() {
        return countryService
                .findAllCountries()
                .doOnSubscribe(sub -> log.info("[question2B] subscription started to findAllCountries()"))
                .doOnNext(country -> log.info("[question2B] emitted country='{}'", country))
                .takeUntil(country -> "Argentina".equals(country))
                .doOnComplete(() -> log.info("[question2B] completed upon finding 'Argentina'"))
                .doOnError(error -> log.error("[question2B] an error occurred", error));
    }

    public Flux<String> question2C() {
        return countryService
                .findAllCountries()
                .doOnSubscribe(sub -> log.info("[question2C] subscription started to findAllCountries()"))
                .doOnNext(country -> log.info("[question2C] emitted country='{}'", country))
                .takeWhile(country -> !"France".equals(country))
                .doOnComplete(() -> log.info("[question2C] completed upon encountering 'France'"))
                .doOnError(error -> log.error("[question2C] an error occurred", error));
    }
}
