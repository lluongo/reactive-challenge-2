package cl.tenpo.learning.reactive.tasks.task1;

import cl.tenpo.learning.reactive.utils.exception.ResourceNotFoundException;
import cl.tenpo.learning.reactive.utils.exception.UserServiceException;
import cl.tenpo.learning.reactive.utils.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class T1Question1 {

    private final UserService userService;

    public Mono<Integer> question1A() {
        return userService
                .findFirstName()
                .doOnSubscribe(sub -> log.info("[question1A] subscription started"))
                .flatMap(fullName -> {
                    if (fullName != null && fullName.startsWith("A")) {
                        int length = fullName.length();
                        log.info("[question1A] '{}' starts with 'A', length={}", fullName, length);
                        return Mono.just(length);
                    } else {
                        log.info("[question1A] '{}' does not start with 'A', emitting -1", fullName);
                        return Mono.just(-1);
                    }
                })
                .doOnError(error -> log.error("[question1A] an error occurred", error))
                .doOnSuccess(result -> log.info("[question1A] completed with result={}", result));
    }

    public Mono<String> question1B() {
        return userService
                .findFirstName()
                .doOnSubscribe(sub -> log.info("[question1B] subscription started to findFirstName()"))
                .flatMap(fullName -> {
                    log.info("[question1B] findFirstName() returned '{}'", fullName);
                    return userService
                            .existByName(fullName)
                            .doOnSubscribe(sub -> log.info("[question1B] subscription started " +
                                                           "to existByName('{}')", fullName))
                            .flatMap(exists -> {
                                Mono<String> actionMono = Boolean.TRUE.equals(exists)
                                        ? userService.update(fullName)
                                        .doOnNext(msg -> log.info("[question1B] '{}' " +
                                                                  "exists. update('{}') " +
                                                                  "returned='{}'", fullName, fullName, msg))
                                        : userService.insert(fullName)
                                        .doOnNext(msg -> log.info("[question1B] '{}' does NOT exist. " +
                                                                  "insert('{}') returned='{}'", fullName, fullName, msg));
                                return actionMono;
                            });
                })
                .doOnError(error -> log.error("[question1B] an error occurred", error))
                .doOnSuccess(finalMsg -> log.info("[question1B] completed with message='{}'", finalMsg));
    }

    public Mono<String> question1C(String name) {
        return userService
                .findFirstByName(name)
                .doOnSubscribe(sub -> log.info("[question1C] subscription " +
                                               "started for findFirstByName('{}')", name))
                .doOnNext(value -> log.info("[question1C] findFirstByName('{}') " +
                                            "returned='{}'", name, value))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[question1C] no user found for '{}', throwing ResourceNotFoundException", name);
                    return Mono.error(new ResourceNotFoundException(name));
                }))
                .doOnError(error -> {
                    if (error instanceof ResourceNotFoundException) {
                        log.warn("[question1C] resource not found for '{}'", name);
                    } else {
                        log.error("[question1C] unexpected error when finding '{}'", name, error);
                    }
                })
                .onErrorMap(error -> !(error instanceof ResourceNotFoundException),
                        error -> new UserServiceException())
                .doOnSuccess(result -> log.info("[question1C] completed with result='{}'", result));
    }
}
