package cl.tenpo.learning.reactive.tasks.task2.infrastructure.factory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Factory para crear respuestas HTTP estandarizadas.
 * Aplica el Factory Pattern para centralizar la creación de respuestas.
 */
@Component
public class ResponseFactory {
    
    public <T> Mono<ServerResponse> success(T body) {
        return ServerResponse.ok().bodyValue(body);
    }
    
    public <T> Mono<ServerResponse> created(T body) {
        return ServerResponse.status(HttpStatus.CREATED).bodyValue(body);
    }
    
    public Mono<ServerResponse> noContent() {
        return ServerResponse.noContent().build();
    }
    
    public Mono<ServerResponse> notFound() {
        return ServerResponse.notFound().build();
    }
   
    public Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest().bodyValue(message);
    }
    
    public Mono<ServerResponse> badRequest(Throwable error) {
        return ServerResponse.badRequest().bodyValue(error.getMessage());
    }
    
    public Mono<ServerResponse> error(String message) {
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(message);
    }
    
    public Mono<ServerResponse> error(Throwable error) {
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(error.getMessage());
    }
}
