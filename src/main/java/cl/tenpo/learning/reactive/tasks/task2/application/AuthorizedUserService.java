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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizedUserService {

    private final AuthorizedUserRepository userRepository;

    public Flux<AuthorizedUser> findAllUsers() {
        return userRepository.findAll()
                .doOnComplete(() -> log.debug("Búsqueda de todos los usuarios completada"))
                .doOnError(error -> log.error("Error al buscar usuarios: {}", error.getMessage()));
    }

    public Mono<AuthorizedUser> findUserById(Long id) {
        return userRepository.findById(id)
                .doOnNext(user -> log.debug("Usuario encontrado con ID {}: {}", id, user.getUsername()))
                .doOnError(error -> log.error("Error al buscar usuario con ID {}: {}", id, error.getMessage()));
    }

    public Mono<AuthorizedUser> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .doOnNext(user -> log.debug("Usuario encontrado con username {}: {}", username, user.getId()))
                .doOnError(error -> log.error("Error al buscar usuario con username {}: {}", username, error.getMessage()));
    }

    public Mono<AuthorizedUser> createUser(AuthorizedUserRequest request) {
        log.info("Creando nuevo usuario autorizado: {}", request.getUsername());
        LocalDateTime now = LocalDateTime.now();
        
        return checkUsernameAvailability(request.getUsername())
                .then(checkEmailAvailability(request.getEmail()))
                .then(Mono.defer(() -> createAndSaveUser(request, now)));
    }

    private Mono<Void> checkUsernameAvailability(String username) {
        return userRepository.findByUsername(username)
                .flatMap(__ -> Mono.<Void>error(new IllegalArgumentException("Username already exists")))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<Void> checkEmailAvailability(String email) {
        return userRepository.findByEmail(email)
                .flatMap(__ -> Mono.<Void>error(new IllegalArgumentException("Email already exists")))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<AuthorizedUser> createAndSaveUser(AuthorizedUserRequest request, LocalDateTime now) {
        return Mono.fromCallable(() -> AuthorizedUser.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .active(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build())
                .flatMap(userRepository::save)
                .doOnNext(user -> log.info("Usuario creado exitosamente: {}", user.getUsername()));
    }

    public Mono<AuthorizedUser> deactivateUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found with ID: " + id)))
                .filter(AuthorizedUser::getActive)
                .switchIfEmpty(Mono.error(new IllegalStateException("User is already inactive")))
                .flatMap(this::deactivateUserInternal);
    }

    private Mono<AuthorizedUser> deactivateUserInternal(AuthorizedUser user) {
        return Mono.just(user)
                .map(this::markAsInactive)
                .flatMap(userRepository::save)
                .doOnNext(u -> log.info("Usuario desactivado: {}", u.getUsername()));
    }

    private AuthorizedUser markAsInactive(AuthorizedUser user) {
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public Mono<Boolean> isUserAuthorized(String username) {
        return userRepository.findByUsername(username)
                .map(AuthorizedUser::getActive)
                .defaultIfEmpty(false)
                .doOnNext(isActive -> log.debug("Usuario {} autorización: {}", username, isActive ? "activo" : "inactivo"));
    }

    /**
     * Encuentra todos los usuarios como una lista
     */
    public Mono<List<AuthorizedUser>> findAllUsersAsList() {
        return findAllUsers()
                .collectList()
                .doOnSuccess(users -> log.info("Recuperados {} usuarios", users.size()));
    }

    /**
     * Activa un usuario por su nombre de usuario
     */
    public Mono<AuthorizedUser> activateUser(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> !user.getActive())  // Solo proceder si el usuario está inactivo
                .switchIfEmpty(Mono.defer(() -> 
                    userRepository.findByUsername(username)
                        .filter(AuthorizedUser::getActive)
                        .flatMap(user -> Mono.just(user))
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado: " + username)))
                .flatMap(user -> {
                    user.setActive(true);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .doOnSuccess(user -> log.info("Usuario activado: {}", user.getUsername()))
                .doOnError(e -> log.error("Error activando usuario: {}", e.getMessage()));
    }

    /**
     * Encuentra un usuario por su ID en formato string
     */
    public Mono<AuthorizedUser> findUserByIdFromString(String idString) {
        return parseIdSafely(idString)
                .flatMap(this::findUserById);
    }

    /**
     * Desactiva un usuario usando su ID en formato string
     */
    public Mono<AuthorizedUser> deactivateUserFromString(String idString) {
        return parseIdSafely(idString)
                .flatMap(this::deactivateUser);
    }
    
    /**
     * Parsea un ID de forma segura
     */
    private Mono<Long> parseIdSafely(String idString) {
        return Mono.fromCallable(() -> Long.parseLong(idString))
                .onErrorMap(NumberFormatException.class, 
                    ex -> new IllegalArgumentException("Invalid user ID format: " + idString));
    }
}
