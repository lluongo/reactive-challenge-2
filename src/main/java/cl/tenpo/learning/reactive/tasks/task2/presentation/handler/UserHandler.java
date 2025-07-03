package cl.tenpo.learning.reactive.tasks.task2.presentation.handler;

import cl.tenpo.learning.reactive.tasks.task2.application.AuthorizedUserService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.AuthorizedUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler funcional para operaciones de usuarios autorizados.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final AuthorizedUserService userService;

    /**
     * Obtiene todos los usuarios autorizados.
     */
    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return userService.findAllUsers()
                .collectList()
                .flatMap(users -> ServerResponse.ok().bodyValue(users))
                .doOnError(error -> log.error("Error getting all users: {}", error.getMessage()))
                .onErrorResume(this::handleServiceError);
    }

    /**
     * Crea un nuevo usuario autorizado.
     */
    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(AuthorizedUserRequest.class)
                .flatMap(userService::createUser)
                .flatMap(user -> ServerResponse.status(201).bodyValue(user))
                .doOnError(error -> log.error("Error creating user: {}", error.getMessage()))
                .onErrorResume(this::handleUserCreationError);
    }

    /**
     * Obtiene un usuario por ID.
     */
    public Mono<ServerResponse> getUserById(ServerRequest request) {
        return Mono.fromCallable(() -> Long.parseLong(request.pathVariable("id")))
                .flatMap(userService::findUserById)
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnError(error -> log.error("Error getting user by id: {}", error.getMessage()))
                .onErrorResume(this::handleServiceError);
    }

    /**
     * Desactiva un usuario.
     */
    public Mono<ServerResponse> deactivateUser(ServerRequest request) {
        return Mono.fromCallable(() -> Long.parseLong(request.pathVariable("id")))
                .flatMap(userService::deactivateUser)
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnError(error -> log.error("Error deactivating user: {}", error.getMessage()))
                .onErrorResume(this::handleServiceError);
    }

    /**
     * Maneja errores de servicio usando operadores reactivos.
     */
    private Mono<ServerResponse> handleServiceError(Throwable error) {
        return ServerResponse.status(500)
                .bodyValue("Error with user service: " + error.getMessage());
    }

    /**
     * Maneja errores específicos de creación de usuarios.
     */
    private Mono<ServerResponse> handleUserCreationError(Throwable error) {
        return Mono.just(error.getMessage())
                .filter(msg -> msg.contains("already exists"))
                .flatMap(msg -> ServerResponse.badRequest()
                        .bodyValue("User already exists: " + msg))
                .switchIfEmpty(ServerResponse.status(500)
                        .bodyValue("Error creating user: " + error.getMessage()));
    }
}
