package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.model.Page;
import cl.tenpo.learning.reactive.utils.service.CountryService;
import cl.tenpo.learning.reactive.utils.service.TranslatorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class T1Question3Test {

    @InjectMocks
    private T1Question3 t1Question3;

    @Mock
    private CountryService countryServiceMock;

    @Mock
    private TranslatorService translatorServiceMock;

    @Test
    @DisplayName("PREGUNTA 3A - Page tiene elementos")
    public void question3_a_uc1_test() {

        List<String> elems = List.of("elem1", "elem2", "elem3", "elem4");
        Page<String> page = new Page<>(elems);

        Flux<String> outputFlux = t1Question3.question3A(page);

        StepVerifier.create(outputFlux.log())
                .expectNext("elem1", "elem2", "elem3", "elem4")
                .verifyComplete();

    }

    @Test
    @DisplayName("PREGUNTA 3A - Page no tiene elementos")
    public void question3_a_uc2_test() {

        List<String> elems = Collections.emptyList();
        Page<String> page = new Page<>(elems);

        Flux<String> outputFlux = t1Question3.question3A(page);

        StepVerifier.create(outputFlux.log())
                .verifyComplete();

    }

    @Test
    @DisplayName("PREGUNTA 3A - Page es nulo")
    public void question3_a_uc3_test() {

        Flux<String> outputFlux = t1Question3.question3A(null);

        StepVerifier.create(outputFlux.log())
                .verifyComplete();

    }

    @Test
    @DisplayName("PREGUNTA 3B - Monedas de Argentina")
    public void question3_b_uc1_test() {

        when(countryServiceMock.findCurrenciesByCountry("Argentina")).thenReturn(Flux.just("ARS", "USD"));

        Flux<String> outputFlux = t1Question3.question3B("Argentina");

        StepVerifier.create(outputFlux.log())
                .expectNext("ARS", "USD")
                .verifyComplete();

        verify(countryServiceMock, times(1)).findCurrenciesByCountry("Argentina");
        verifyNoMoreInteractions(countryServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 3C - Todos los países tienen traducción")
    public void question3_c_uc1_test() {

        when(countryServiceMock.findAllCountries()).thenReturn(Flux.just("Argentina", "Brazil", "Switzerland"));
        when(translatorServiceMock.translate("Argentina")).thenReturn("Argentina");
        when(translatorServiceMock.translate("Brazil")).thenReturn("Brasil");
        when(translatorServiceMock.translate("Switzerland")).thenReturn("Suiza");

        Flux<String> outputFlux = t1Question3.question3C();

        StepVerifier.create(outputFlux.log())
                .expectNext("Argentina", "Brasil", "Suiza")
                .verifyComplete();

        verify(countryServiceMock, times(1)).findAllCountries();
        verifyNoMoreInteractions(countryServiceMock);
        verify(translatorServiceMock, times(1)).translate("Argentina");
        verify(translatorServiceMock, times(1)).translate("Brazil");
        verify(translatorServiceMock, times(1)).translate("Switzerland");
        verifyNoMoreInteractions(translatorServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 3C - Al menos un país no tiene traducción")
    public void question3_c_uc2_test() {

        when(countryServiceMock.findAllCountries()).thenReturn(Flux.just("Argentina", "Brazil", "Switzerland"));
        when(translatorServiceMock.translate("Argentina")).thenReturn("Argentina");
        when(translatorServiceMock.translate("Brazil")).thenReturn(null);
        when(translatorServiceMock.translate("Switzerland")).thenReturn("Suiza");

        Flux<String> outputFlux = t1Question3.question3C();

        StepVerifier.create(outputFlux.log())
                .expectNext("Argentina", "Suiza")
                .verifyComplete();

        verify(countryServiceMock, times(1)).findAllCountries();
        verifyNoMoreInteractions(countryServiceMock);
        verify(translatorServiceMock, times(1)).translate("Argentina");
        verify(translatorServiceMock, times(1)).translate("Brazil");
        verify(translatorServiceMock, times(1)).translate("Switzerland");
        verifyNoMoreInteractions(translatorServiceMock);

    }

}
