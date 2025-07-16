package cl.tenpo.learning.reactive.tasks.task2.presentation.handler;

import cl.tenpo.learning.reactive.tasks.task2.application.port.AuthorizedUserService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.factory.ResponseFactory;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.AuthorizedUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor

public class UserHandler {

    private final AuthorizedUserService userService;
    private final ResponseFactory responseFactory;

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return userService.findAllUsersAsList()
                .flatMap(responseFactory::success)
                .doOnError(error -> log.error("Error getting all users: {}", error.getMessage()))
                .onErrorResume(responseFactory::error);
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(AuthorizedUserRequest.class)
                .flatMap(userService::createUser)
                .flatMap(responseFactory::created)
                .doOnError(error -> log.error("Error creating user: {}", error.getMessage()))
                .onErrorResume(responseFactory::error);
    }

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        return userService.findUserByIdFromString(request.pathVariable("id"))
                .flatMap(responseFactory::success)
                .switchIfEmpty(responseFactory.notFound())
                .doOnError(error -> log.error("Error getting user by id: {}", error.getMessage()))
                .onErrorResume(responseFactory::error);
    }

    public Mono<ServerResponse> deactivateUser(ServerRequest request) {
        return userService.deactivateUserFromString(request.pathVariable("id"))
                .flatMap(responseFactory::success)
                .switchIfEmpty(responseFactory.notFound())
                .doOnError(error -> log.error("Error deactivating user: {}", error.getMessage()))
                .onErrorResume(responseFactory::error);
    }
}
