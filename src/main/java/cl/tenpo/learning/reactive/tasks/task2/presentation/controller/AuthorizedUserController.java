package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.AuthorizedUserService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.AuthorizedUser;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.AuthorizedUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthorizedUserController {

    private final AuthorizedUserService userService;
    
    @GetMapping("${app.api.endpoints.users}")
    public Flux<AuthorizedUser> getAllUsers() {
        log.info("Request to get all authorized users");
        return userService.findAllUsers();
    }

    @PostMapping("${app.api.endpoints.users}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthorizedUser> createUser(@Valid @RequestBody AuthorizedUserRequest request) {
        log.info("Request to create authorized user: {}", request);
        return userService.createUser(request);
    }

    @DeleteMapping("${app.api.endpoints.users}/{id}")
    public Mono<AuthorizedUser> deactivateUser(@PathVariable Long id) {
        log.info("Request to deactivate user with id: {}", id);
        return userService.deactivateUser(id);
    }

    @GetMapping("${app.api.endpoints.users}/{id}")
    public Mono<AuthorizedUser> getUserById(@PathVariable Long id) {
        log.info("Request to get user with id: {}", id);
        return userService.findUserById(id);
    }
    
    @PostMapping("${app.api.endpoints.users}/{username}/activate")
    public Mono<AuthorizedUser> activateUser(@PathVariable String username) {
        log.info("Request to activate user: {}", username);
        return userService.activateUser(username);
    }
}
