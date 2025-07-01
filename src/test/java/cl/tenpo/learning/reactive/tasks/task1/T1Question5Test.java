package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.service.CalculatorService;
import cl.tenpo.learning.reactive.utils.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class T1Question5Test {

    @InjectMocks
    private T1Question5 t1Question5;

    @Mock
    private CalculatorService calculatorServiceMock;

    @Mock
    private UserService userServiceMock;

    @Test
    @DisplayName("PREGUNTA 5A - Los cálculos salen bien")
    public void question5_a_uc1_test() {

        when(calculatorServiceMock.calculate(any()))
                .thenReturn(Mono.just(BigDecimal.TEN));

        when(userServiceMock.findFirstName()).thenReturn(Mono.just("Juan"));

        Mono<String> outputMono = t1Question5.question5A();

        StepVerifier.create(outputMono.log())
                .expectNext("Juan")
                .verifyComplete();

        for (int i = 100; i <= 1000; i++) {
            verify(calculatorServiceMock, times(1)).calculate(BigDecimal.valueOf(i));
        }

        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(99));
        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(1001));
        verify(userServiceMock, times(1)).findFirstName();

    }

    @Test
    @DisplayName("PREGUNTA 5A - Los cálculos retornan vacío")
    public void question5_a_uc2_test() {

        when(calculatorServiceMock.calculate(any())).thenReturn(Mono.empty());

        when(userServiceMock.findFirstName()).thenReturn(Mono.just("Juan"));

        Mono<String> outputMono = t1Question5.question5A();

        StepVerifier.create(outputMono.log())
                .expectNext("Juan")
                .verifyComplete();

        for (int i = 100; i <= 1000; i++) {
            verify(calculatorServiceMock, times(1)).calculate(BigDecimal.valueOf(i));
        }

        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(99));
        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(1001));
        verify(userServiceMock, times(1)).findFirstName();

    }

    @Test
    @DisplayName("PREGUNTA 5A - Los cálculos salen mal")
    public void question5_a_uc3_test() {

        when(calculatorServiceMock.calculate(any())).thenReturn(Mono.empty());

        when(calculatorServiceMock.calculate(BigDecimal.valueOf(500)))
                .thenReturn(Mono.error(new RuntimeException("oops")));

        Mono<String> outputMono = t1Question5.question5A();

        StepVerifier.create(outputMono.log())
                .expectNext("Chuck Norris")
                .verifyComplete();

        for (int i = 100; i <= 500; i++) {
            verify(calculatorServiceMock, times(1)).calculate(BigDecimal.valueOf(i));
        }

        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(99));
        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(501));
        verify(userServiceMock, never()).findFirstName();

    }

    @Test
    @DisplayName("PREGUNTA 5B - Los cálculos salen bien")
    public void question5_b_uc1_test() {

        when(calculatorServiceMock.calculate(any()))
                .thenReturn(Mono.just(BigDecimal.TEN));

        when(userServiceMock.findAllNames()).thenReturn(Flux.just("Miguel", "Juan", "María", "Pepe", "Sofía"));

        Flux<String> outputFlux = t1Question5.question5B();

        StepVerifier.create(outputFlux.log())
                .expectNext("Miguel", "Juan", "María")
                .verifyComplete();

        for (int i = 100; i <= 1000; i++) {
            verify(calculatorServiceMock, times(1)).calculate(BigDecimal.valueOf(i));
        }

        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(99));
        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(1001));
        verify(userServiceMock, times(1)).findAllNames();

    }

    @Test
    @DisplayName("PREGUNTA 5B - Los cálculos retornan vacío")
    public void question5_b_uc2_test() {

        when(calculatorServiceMock.calculate(any())).thenReturn(Mono.empty());

        when(userServiceMock.findAllNames()).thenReturn(Flux.just("Miguel", "Juan", "María", "Pepe", "Sofía"));

        Flux<String> outputFlux = t1Question5.question5B();

        StepVerifier.create(outputFlux.log())
                .expectNext("Miguel", "Juan", "María")
                .verifyComplete();

        for (int i = 100; i <= 1000; i++) {
            verify(calculatorServiceMock, times(1)).calculate(BigDecimal.valueOf(i));
        }

        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(99));
        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(1001));
        verify(userServiceMock, times(1)).findAllNames();

    }

    @Test
    @DisplayName("PREGUNTA 5B - Los cálculos salen mal")
    public void question5_b_uc3_test() {

        when(calculatorServiceMock.calculate(any())).thenReturn(Mono.empty());

        when(calculatorServiceMock.calculate(BigDecimal.valueOf(500)))
                .thenReturn(Mono.error(new RuntimeException("oops")));

        Flux<String> outputFlux = t1Question5.question5B();

        StepVerifier.create(outputFlux.log())
                .verifyComplete();

        for (int i = 100; i <= 500; i++) {
            verify(calculatorServiceMock, times(1)).calculate(BigDecimal.valueOf(i));
        }

        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(99));
        verify(calculatorServiceMock, never()).calculate(BigDecimal.valueOf(501));
        verify(userServiceMock, never()).findAllNames();

    }

}
