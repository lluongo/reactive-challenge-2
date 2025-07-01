package cl.tenpo.learning.reactive.modules.module1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@RestController
class BlockingController {

    @GetMapping("/blocking")
    public ResponseEntity<List<String>> demoBlocking() throws InterruptedException, ExecutionException {
        int users = 30;
        ExecutorService executor = Executors.newFixedThreadPool(users);
        List<Callable<String>> tasks = new ArrayList<>();


        for (int i = 1; i <= users; i++) {
            final int userId = i;
            tasks.add(() -> {
                Thread.sleep(500);
                return "User " + userId;
            });
        }

        long start = System.currentTimeMillis();
        List<Future<String>> futures = executor.invokeAll(tasks);
        List<String> resultados = new ArrayList<>();
        for (Future<String> future : futures) {
            resultados.add(future.get());
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Tiempo total blocking paralelo: " + elapsed + " ms");

        executor.shutdown();
        return ResponseEntity.ok(resultados);
    }

}

