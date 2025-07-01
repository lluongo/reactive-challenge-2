package cl.tenpo.learning.reactive.utils.service;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
public class CountryService {

    public Flux<String> findAllCountries() {
        return Flux.interval(Duration.ZERO)
                .map(i -> ModuleUtils.faker().country().name());
    }

    public Flux<String> findCurrenciesByCountry(String country) {
        return Flux.range(0, 2)
                .map(i -> ModuleUtils.faker().country().currencyCode());
    }

}
