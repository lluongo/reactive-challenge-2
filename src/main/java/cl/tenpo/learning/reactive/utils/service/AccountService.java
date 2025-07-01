package cl.tenpo.learning.reactive.utils.service;

import cl.tenpo.learning.reactive.utils.model.Account;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccountService {

    public Mono<Account> getAccountByUserId(String userId) {
        return Mono.just(new Account("ACC-" + userId, userId, Math.random() * 5000));
    }

}
