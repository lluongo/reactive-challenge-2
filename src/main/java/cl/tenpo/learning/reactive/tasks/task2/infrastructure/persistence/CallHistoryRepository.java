package cl.tenpo.learning.reactive.tasks.task2.infrastructure.persistence;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CallHistoryRepository extends ReactiveMongoRepository<CallHistory, String> {
    Flux<CallHistory> findAllBy(Pageable pageable);
}
