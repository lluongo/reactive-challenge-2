package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.model.Page;
import cl.tenpo.learning.reactive.utils.service.CountryService;
import cl.tenpo.learning.reactive.utils.service.TranslatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question3 {

    private final CountryService countryService;
    private final TranslatorService translatorService;

    public Flux<String> question3A(Page<String> page) {
        if (page == null) {
            log.warn("[question3A] received null Page, returning empty Flux");
            return Flux.<String>empty()
                    .doOnSubscribe(sub -> log.info("[question3A] subscription started on empty Flux"))
                    .doOnComplete(() -> log.info("[question3A] completed with no elements (page was null)"));
        }

        return Flux.fromIterable(page.items())
                .doOnSubscribe(sub -> log.info("[question3A] subscription started to emit page items"))
                .doOnNext(item -> log.info("[question3A] emitting item='{}'", item))
                .doOnComplete(() -> log.info("[question3A] completed emitting all items"))
                .doOnError(error -> log.error("[question3A] unexpected error while emitting items", error));
    }

    public Flux<String> question3B(String country) {
        return countryService
                .findCurrenciesByCountry(country)
                .doOnSubscribe(sub ->
                        log.info("[question3B] subscription started to findCurrenciesByCountry('{}')", country))
                .distinct()
                .doOnNext(currency ->
                        log.info("[question3B] distinct currency='{}'", currency))
                .doOnComplete(() ->
                        log.info("[question3B] completed emitting all distinct currencies for '{}'", country))
                .doOnError(error ->
                        log.error("[question3B] an error occurred while fetching currencies for '{}'", country, error));
    }

    public Flux<String> question3C() {
        return countryService
                .findAllCountries()
                .take(3)
                .doOnSubscribe(sub -> log.info("[question3B] subscription started to findAllCountries()"))
                .flatMap(country -> {
                    log.info("[question3C] processing country='{}'", country);
                    String translated = translatorService.translate(country);
                    if (translated == null) {
                        log.warn("[question3C] no translation available for '{}', skipping", country);
                        return Mono.empty();
                    }
                    log.info("[question3C] translation for '{}' is '{}'", country, translated);
                    return Mono.just(translated);
                })
                .doOnComplete(() -> log.info("[question3C] completed emitting translations"))
                .doOnError(error -> log.error("[question3C] an error occurred", error));
    }

}
