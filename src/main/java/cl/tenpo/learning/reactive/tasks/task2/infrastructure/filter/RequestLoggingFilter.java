package cl.tenpo.learning.reactive.tasks.task2.infrastructure.filter;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestLoggingFilter implements WebFilter {

    private final CallHistoryService callHistoryService;
    private final ObjectMapper objectMapper;
    
    private final List<String> excludedPaths = Arrays.asList("/actuator", "/swagger", "/v3/api-docs");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }
        log.info("Incoming request: {} {}", method, path);
        ResponseCaptureExchange responseCapture = new ResponseCaptureExchange(exchange);
        
        return chain.filter(responseCapture)
                .then(Mono.defer(() -> {
                    int statusCode = responseCapture.getResponse().getStatusCode() != null 
                        ? responseCapture.getResponse().getStatusCode().value() 
                        : 200;
                    
                    String responseBody = responseCapture.getResponseBody();
                    String requestBody = responseCapture.getRequestBody();
                    log.info("Response status: {} for {} {}", statusCode, method, path);
                    
                    return recordRequest(path, method, requestBody, responseBody, statusCode);
                }));
    }

    private boolean isExcludedPath(String path) {
        return excludedPaths.stream().anyMatch(path::startsWith);
    }
    @SneakyThrows
    private Mono<Void> recordRequest(String endpoint, String method, String parameters, String response, int statusCode) {
        return Mono.just(statusCode)
                .filter(status -> status >= 200 && status < 300)
                .flatMap(status -> callHistoryService.recordSuccessfulRequest(endpoint, method, parameters, response))
                .switchIfEmpty(callHistoryService.recordFailedRequest(endpoint, method, parameters, response))
                .then();
    }
}
