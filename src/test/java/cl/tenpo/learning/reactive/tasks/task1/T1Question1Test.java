package cl.tenpo.learning.reactive.tasks.task1;


import cl.tenpo.learning.reactive.utils.exception.ResourceNotFoundException;
import cl.tenpo.learning.reactive.utils.exception.UserServiceException;
import cl.tenpo.learning.reactive.utils.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class T1Question1Test {

    @InjectMocks
    private T1Question1 t1Question1;

    @Mock
    private UserService userServiceMock;

    @Test
    @DisplayName("PREGUNTA 1A - Nombre arranca con A")
    public void question1_a_uc1_test() {

        when(userServiceMock.findFirstName()).thenReturn(Mono.just("Alan"));

        Mono<Integer> outputMono = t1Question1.question1A();

        StepVerifier.create(outputMono.log())
                .expectNext(4)
                .verifyComplete();

        verify(userServiceMock, times(1)).findFirstName();
        verifyNoMoreInteractions(userServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 1A - Nombre arranca con M")
    public void question1_a_uc2_test() {

        when(userServiceMock.findFirstName()).thenReturn(Mono.just("Miguel"));

        Mono<Integer> outputMono = t1Question1.question1A();

        StepVerifier.create(outputMono.log())
                .expectNext(-1)
                .verifyComplete();

        verify(userServiceMock, times(1)).findFirstName();
        verifyNoMoreInteractions(userServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 1B - Nombre existe en la DB")
    public void question1_b_uc1_test() {

        String name = "Juan";
        String expectedOutput = "UPDATED";
        when(userServiceMock.findFirstName()).thenReturn(Mono.just(name));
        when(userServiceMock.existByName(name)).thenReturn(Mono.just(true));
        when(userServiceMock.update(name)).thenReturn(Mono.just(expectedOutput));

        Mono<String> outputMono = t1Question1.question1B();

        StepVerifier.create(outputMono.log())
                .expectNext(expectedOutput)
                .verifyComplete();

        verify(userServiceMock, times(1)).existByName(name);
        verify(userServiceMock, times(1)).update(name);
        verify(userServiceMock, never()).insert(any());
        verifyNoMoreInteractions(userServiceMock);


    }

    @Test
    @DisplayName("PREGUNTA 1B - Nombre no existe en la DB")
    public void question1_b_uc2_test() {

        String name = "Juan";
        String expectedOutput = "INSERTED";
        when(userServiceMock.findFirstName()).thenReturn(Mono.just(name));
        when(userServiceMock.existByName(name)).thenReturn(Mono.just(false));
        when(userServiceMock.insert(name)).thenReturn(Mono.just(expectedOutput));

        Mono<String> outputMono = t1Question1.question1B();

        StepVerifier.create(outputMono.log())
                .expectNext(expectedOutput)
                .verifyComplete();

        verify(userServiceMock, times(1)).existByName(name);
        verify(userServiceMock, times(1)).insert(name);
        verify(userServiceMock, never()).update(any());
        verifyNoMoreInteractions(userServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 1C - El servicio devuelve un registro")
    public void question1_c_uc1_test() {

        String name = "Juan";
        when(userServiceMock.findFirstByName(name)).thenReturn(Mono.just(name));

        Mono<String> outputMono = t1Question1.question1C(name);

        StepVerifier.create(outputMono.log())
                .expectNext(name)
                .verifyComplete();

        verify(userServiceMock, times(1)).findFirstByName(name);
        verifyNoMoreInteractions(userServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 1C - El servicio devuelve un vacío")
    public void question1_c_uc2_test() {

        String name = "Juan";
        when(userServiceMock.findFirstByName(name)).thenReturn(Mono.empty());

        Mono<String> outputMono = t1Question1.question1C(name);

        StepVerifier.create(outputMono.log())
                .verifyError(ResourceNotFoundException.class);

        verify(userServiceMock, times(1)).findFirstByName(name);
        verifyNoMoreInteractions(userServiceMock);

    }

    @Test
    @DisplayName("PREGUNTA 1C - El servicio arroja un error")
    public void question1_c_uc3_test() {

        String name = "Juan";
        when(userServiceMock.findFirstByName(name)).thenReturn(Mono.error(new RuntimeException("oops")));

        Mono<String> outputMono = t1Question1.question1C(name);

        StepVerifier.create(outputMono.log())
                .verifyError(UserServiceException.class);

        verify(userServiceMock, times(1)).findFirstByName(name);
        verifyNoMoreInteractions(userServiceMock);

    }
}
