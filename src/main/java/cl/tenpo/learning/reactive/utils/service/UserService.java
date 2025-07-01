package cl.tenpo.learning.reactive.utils.service;

import cl.tenpo.learning.reactive.utils.ModuleUtils;
import cl.tenpo.learning.reactive.utils.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserService {

    public Flux<String> findAllNames() {
        return Flux.interval(Duration.ZERO)
                .map(i -> ModuleUtils.faker().name().fullName());
    }

    public Mono<String> findFirstName() {
        return findAllNames()
                .next();
    }

    public Mono<User> getUserById(String userId) {
        return Mono.just(new User(userId, ModuleUtils.faker().name().firstName()));
    }

    public Mono<String> findFirstByName(String name) {
        return Mono.just(name);
    }

    // simulate DB exists
    public Mono<Boolean> existByName(String name) {
        return Mono.just(name)
                .map(n -> Math.random() > 0.5);
    }

    // simulate DB update
    public Mono<String> update(String name) {
        return Mono.just("Updated: " + name);
    }

    // simulate DB insert
    public Mono<String> insert(String name) {
        return Mono.just("Inserted: " + name);
    }

}
