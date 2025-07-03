package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.AuthorizedUser;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.persistence.AuthorizedUserRepository;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.AuthorizedUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizedUserService {

    private final AuthorizedUserRepository userRepository;

    public Flux<AuthorizedUser> findAllUsers() {
        return userRepository.findAll();
    }

    public Mono<AuthorizedUser> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<AuthorizedUser> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<AuthorizedUser> createUser(AuthorizedUserRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return userRepository.findByUsername(request.getUsername())
                .flatMap(existingUser -> Mono.<AuthorizedUser>error(
                        new IllegalArgumentException("Username already exists")))
                .switchIfEmpty(userRepository.findByEmail(request.getEmail())
                        .flatMap(existingUser -> Mono.<AuthorizedUser>error(
                                new IllegalArgumentException("Email already exists")))
                        .switchIfEmpty(Mono.defer(() -> {
                            AuthorizedUser newUser = AuthorizedUser.builder()
                                    .username(request.getUsername())
                                    .email(request.getEmail())
                                    .active(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                            log.info("Creating new authorized user: {}", newUser);
                            return userRepository.save(newUser);
                        })));
    }

    public Mono<AuthorizedUser> deactivateUser(Long id) {
        return userRepository.findById(id)
                .filter(AuthorizedUser::getActive)
                .switchIfEmpty(Mono.error(new IllegalStateException("User is already inactive or not found")))
                .flatMap(this::deactivateUserInternal)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found with ID: " + id)));
    }

    /**
     * Desactiva un usuario internamente usando operadores reactivos.
     */
    private Mono<AuthorizedUser> deactivateUserInternal(AuthorizedUser user) {
        return Mono.just(user)
                .doOnNext(u -> {
                    u.setActive(false);
                    u.setUpdatedAt(LocalDateTime.now());
                    log.info("Deactivating user: {}", u);
                })
                .flatMap(userRepository::save);
    }

    public Mono<Boolean> isUserAuthorized(String username) {
        return userRepository.findByUsername(username)
                .map(AuthorizedUser::getActive)
                .defaultIfEmpty(false);
    }
}
