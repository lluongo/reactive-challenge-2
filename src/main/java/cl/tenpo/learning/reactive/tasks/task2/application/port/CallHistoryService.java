package cl.tenpo.learning.reactive.tasks.task2.application.port;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
public interface CallHistoryService {
    
    Flux<CallHistory> getCallHistory(Pageable pageable);
    
    Mono<List<CallHistory>> getCallHistoryAsList(Pageable pageable);
    
    Mono<List<CallHistory>> getCallHistoryFromParams(Integer page, Integer size);
    
    Mono<CallHistory> recordSuccessfulRequest(String endpoint, String method, String parameters, String response);
    
    Mono<CallHistory> recordFailedRequest(String endpoint, String method, String parameters, String error);
}
