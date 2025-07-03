package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/debug")
public class DebugController {

    private final ApplicationContext applicationContext;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @GetMapping("/routes")
    public Mono<Map<String, String>> getRoutes() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        
        Map<String, String> routes = new HashMap<>();
        
        map.forEach((key, value) -> {
            routes.put(key.toString(), value.toString());
        });
        
        return Mono.just(routes);
    }
    
    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("pong");
    }
    
    @DeleteMapping("/clear-cache")
    public Mono<Map<String, String>> clearRedisCache() {
        return reactiveRedisTemplate.opsForValue().delete(RedisConfig.PERCENTAGE_KEY)
                .map(result -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Redis cache cleared");
                    response.put("keysDeleted", result.toString());
                    return response;
                });
    }
    
    @PostMapping("/clear-cache")
    public Mono<Map<String, String>> clearRedisCachePost() {
        return clearRedisCache();
    }
}
