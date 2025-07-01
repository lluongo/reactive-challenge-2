package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.model.Account;
import cl.tenpo.learning.reactive.utils.model.User;
import cl.tenpo.learning.reactive.utils.model.UserAccount;
import cl.tenpo.learning.reactive.utils.service.AccountService;
import cl.tenpo.learning.reactive.utils.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class T1Question7Test {

    @InjectMocks
    private T1Question7 t1Question7;

    @Mock
    private UserService userServiceMock;

    @Mock
    private AccountService accountServiceMock;

    @Test
    @DisplayName("PREGUNTA 7 - User Account")
    void question7_uc1_test() {
        String userId = "123";
        User user = new User(userId, "Miguel");
        Account account = new Account("ACC-" + userId, userId, 150000.00);

        when(userServiceMock.getUserById(userId)).thenReturn(Mono.just(user));
        when(accountServiceMock.getAccountByUserId(userId)).thenReturn(Mono.just(account));

        Mono<UserAccount> result = t1Question7.question7(userId);

        StepVerifier.create(result.log())
                .expectSubscription()
                .assertNext(accountStatus -> {
                    assertEquals(userId, accountStatus.user().id());
                    assertEquals(userId, accountStatus.account().userId());
                })
                .verifyComplete();

        verify(userServiceMock, times(1)).getUserById("123");
        verifyNoMoreInteractions(userServiceMock);
        verify(accountServiceMock, times(1)).getAccountByUserId("123");
        verifyNoMoreInteractions(accountServiceMock);
    }

}
