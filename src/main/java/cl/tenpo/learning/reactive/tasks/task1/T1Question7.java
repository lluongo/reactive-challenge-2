package cl.tenpo.learning.reactive.tasks.task1;


import cl.tenpo.learning.reactive.utils.model.UserAccount;
import cl.tenpo.learning.reactive.utils.service.AccountService;
import cl.tenpo.learning.reactive.utils.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question7 {

    private final UserService userService;
    private final AccountService accountService;

    public Mono<UserAccount> question7(String userId) {
        return Mono
                .zip(
                        userService.getUserById(userId)
                                .doOnSubscribe(sub -> log.info("[question7] fetching User for id='{}'", userId))
                                .doOnError(err -> log.error("[question7] error fetching User for id='{}'", userId, err)),
                        accountService.getAccountByUserId(userId)
                                .doOnSubscribe(sub -> log.info("[question7] fetching Account for userId='{}'", userId))
                                .doOnError(err -> log.error("[question7] error fetching Account for userId='{}'", userId, err)),
                        (user, account) -> {
                            log.info("[question7] combining User(id='{}') and Account(id='{}')",
                                    user.id(), account.accountId());
                            return new UserAccount(user, account);
                        }
                )
                .doOnError(err -> log.error("[question7] failed to generate UserAccount for id='{}'", userId, err));
    }
}
