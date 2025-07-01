package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.service.CountryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class T1Question4Test {

    @InjectMocks
    private T1Question4 t1Question4;

    @Mock
    private CountryService countryServiceMock;

    @Test
    @DisplayName("PREGUNTA 4A - Países del mundo")
    public void question4_a_uc1_test() {

        when(countryServiceMock.findAllCountries()).thenReturn(allCountriesMocked());

        Flux<String> outputFlux = t1Question4.question4A();

        StepVerifier.create(outputFlux.log())
                .expectNextSequence(getUniqueCountries())
                .verifyComplete();

        verify(countryServiceMock, times(1)).findAllCountries();
        verifyNoMoreInteractions(countryServiceMock);

    }

    // Helper functions

    private Flux<String> allCountriesMocked() {
        Random random = new Random();
        List<String> uniqueCountries = getUniqueCountries();
        return Flux.generate(() -> uniqueCountries.size()/2, (i, sink) -> {
           sink.next(uniqueCountries.get(i));
           return random.nextInt(0, uniqueCountries.size());
        });
    }

    private List<String> getUniqueCountries() {
        return List.of("Argentina", "Brazil", "China", "Croatia", "Cuba", "Denmark", "India",
                "Japan", "Portugal", "Russia", "Spain", "Switzerland");
    }

}
