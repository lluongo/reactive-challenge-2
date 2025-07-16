package cl.tenpo.learning.reactive.tasks.task2.application.port;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.AuthorizedUser;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.AuthorizedUserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AuthorizedUserService {
    
    Flux<AuthorizedUser> findAllUsers();
    
    Mono<AuthorizedUser> findUserById(Long id);
    
    Mono<AuthorizedUser> findUserByUsername(String username);
    
    Mono<AuthorizedUser> createUser(AuthorizedUserRequest request);
    
    Mono<AuthorizedUser> deactivateUser(Long id);
    
    Mono<Boolean> isUserAuthorized(String username);
    
    Mono<List<AuthorizedUser>> findAllUsersAsList();
    
    Mono<AuthorizedUser> activateUser(String username);
    
    Mono<AuthorizedUser> findUserByIdFromString(String idString);
    
    Mono<AuthorizedUser> deactivateUserFromString(String idString);
}
