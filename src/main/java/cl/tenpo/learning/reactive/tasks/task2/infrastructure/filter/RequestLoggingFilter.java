package cl.tenpo.learning.reactive.tasks.task2.infrastructure.filter;

import cl.tenpo.learning.reactive.tasks.task2.application.CallHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestLoggingFilter implements WebFilter {

    private final CallHistoryService callHistoryService;
    private final ObjectMapper objectMapper;
    
    private static final List<String> EXCLUDED_PATHS = List.of("/actuator", "/swagger", "/v3/api-docs");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // Skip logging for excluded paths
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        log.debug("Incoming request: {} {}", request.getMethod(), path);
        
        // Create a decorator to capture the response
        ResponseCaptureExchange decoratedExchange = new ResponseCaptureExchange(exchange);
        
        return chain.filter(decoratedExchange)
                .doOnSuccess(v -> {
                    try {
                        // Get captured request and response data
                        String requestBody = decoratedExchange.getRequestBody();
                        String responseBody = decoratedExchange.getResponseBody();
                        int statusCode = decoratedExchange.getStatusCode();
                        
                        log.debug("Response for {} {}: status={}, body={}", 
                                request.getMethod(), path, statusCode, responseBody);
                        
                        // Record the successful request
                        if (statusCode >= 200 && statusCode < 300) {
                            callHistoryService.recordSuccessfulRequest(
                                    path,
                                    request.getMethod().name(),
                                    requestBody,
                                    responseBody)
                                    .subscribe();
                        } else {
                            callHistoryService.recordFailedRequest(
                                    path,
                                    request.getMethod().name(),
                                    requestBody, 
                                    "HTTP Status: " + statusCode + ", Response: " + responseBody)
                                    .subscribe();
                        }
                    } catch (Exception e) {
                        log.error("Error recording request history", e);
                    }
                })
                .doOnError(error -> {
                    try {
                        String requestBody = decoratedExchange.getRequestBody();
                        
                        log.error("Error processing request {} {}: {}", 
                                request.getMethod(), path, error.getMessage());
                        
                        // Record the failed request
                        callHistoryService.recordFailedRequest(
                                path,
                                request.getMethod().name(),
                                requestBody,
                                error.getMessage())
                                .subscribe();
                    } catch (Exception e) {
                        log.error("Error recording request history", e);
                    }
                });
    }
}
