package cl.tenpo.learning.reactive.tasks.task2.infrastructure.persistence;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.AuthorizedUser;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AuthorizedUserRepository extends R2dbcRepository<AuthorizedUser, Long> {
    Mono<AuthorizedUser> findByUsername(String username);
    Mono<AuthorizedUser> findByEmail(String email);
}
