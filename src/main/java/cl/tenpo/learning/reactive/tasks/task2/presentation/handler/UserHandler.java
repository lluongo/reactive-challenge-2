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
 * Delega toda la lógica de negocio al servicio correspondiente.
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
        return userService.findAllUsersAsList()
                .flatMap(users -> ServerResponse.ok().bodyValue(users))
                .doOnError(error -> log.error("Error getting all users: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.status(500)
                        .bodyValue("Error retrieving users: " + error.getMessage())
                );
    }

    /**
     * Crea un nuevo usuario autorizado.
     */
    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(AuthorizedUserRequest.class)
                .flatMap(userService::createUser)
                .flatMap(user -> ServerResponse.status(201).bodyValue(user))
                .doOnError(error -> log.error("Error creating user: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.badRequest()
                        .bodyValue("Error creating user: " + error.getMessage())
                );
    }

    /**
     * Obtiene un usuario por ID.
     */
    public Mono<ServerResponse> getUserById(ServerRequest request) {
        return userService.findUserByIdFromString(request.pathVariable("id"))
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnError(error -> log.error("Error getting user by id: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.badRequest()
                        .bodyValue("Error retrieving user: " + error.getMessage())
                );
    }

    /**
     * Desactiva un usuario.
     */
    public Mono<ServerResponse> deactivateUser(ServerRequest request) {
        return userService.deactivateUserFromString(request.pathVariable("id"))
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnError(error -> log.error("Error deactivating user: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.badRequest()
                        .bodyValue("Error deactivating user: " + error.getMessage())
                );
    }
}
