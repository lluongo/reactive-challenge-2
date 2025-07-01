package cl.tenpo.learning.reactive.modules.module1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@RestController
class ReactiveController {

    @GetMapping("/reactive")
    public Mono<ResponseEntity<List<String>>> demoReactive() {
        int users = 30;
        long start = System.currentTimeMillis();

        Flux<String> userFlux = Flux.range(1, users)
                .flatMap(userId ->
                                Mono.delay(Duration.ofMillis(500))
                                        .map(delay -> "User " + userId)
                );

        return userFlux.collectList().map(resultados -> {
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Tiempo total reactive: " + elapsed + " ms");
            return ResponseEntity.ok(resultados);
        });
    }
}
