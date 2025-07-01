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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class T1Question2Test {

    @InjectMocks
    private T1Question2 t1Question2;

    @Mock
    private CountryService countryServiceMock;

    @Test
    @DisplayName("PREGUNTA 2A - Primeros 5 paises son repetidos")
    public void question2_a_uc1_test() {

        List<String> countries = List.of("Peru", "Peru", "Peru", "Peru", "Peru", "Argentina", "Chile", "Ecuador", "Brasil", "Honduras");
        when(countryServiceMock.findAllCountries()).thenReturn(Flux.fromIterable(countries));

        Flux<String> outputFlux = t1Question2.question2A();

        StepVerifier.create(outputFlux.log())
                .expectNext("Peru", "Argentina", "Chile", "Ecuador", "Brasil")
                .verifyComplete();

        verify(countryServiceMock, times(1)).findAllCountries();
        verifyNoMoreInteractions(countryServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 2B - Argentina es el quinto país")
    public void question2_b_uc1_test() {

        List<String> countries = List.of("Peru", "Chile", "Ecuador", "Peru", "Argentina", "Honduras", "Brazil");
        when(countryServiceMock.findAllCountries()).thenReturn(Flux.fromIterable(countries));

        Flux<String> outputFlux = t1Question2.question2B();

        StepVerifier.create(outputFlux.log())
                .expectNext("Peru", "Chile", "Ecuador", "Peru", "Argentina")
                .verifyComplete();

        verify(countryServiceMock, times(1)).findAllCountries();
        verifyNoMoreInteractions(countryServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 2C - Francia es el segundo país")
    public void question2_c_uc1_test() {

        List<String> countries = List.of("Argentina", "France", "Croatia", "Morocco");
        when(countryServiceMock.findAllCountries()).thenReturn(Flux.fromIterable(countries));

        Flux<String> outputFlux = t1Question2.question2C();

        StepVerifier.create(outputFlux.log())
                .expectNext("Argentina")
                .verifyComplete();

        verify(countryServiceMock, times(1)).findAllCountries();
        verifyNoMoreInteractions(countryServiceMock);

    }

}
