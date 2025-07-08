package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.AuthorizedUserService;
import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class HistoryController {
    
    private final CallHistoryService callHistoryService;
    private final AuthorizedUserService userService;

    @GetMapping("${app.api.endpoints.history}")
    public Flux<CallHistory> getHistory(
            @RequestParam(required = true) String username,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching call history for user: {}, page: {}, size: {}", username, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        return userService.isUserAuthorized(username)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no autorizado para acceder al historial")))
                .flatMapMany(authorized -> callHistoryService.getCallHistory(pageable))
                .limitRate(100) 
                .onBackpressureBuffer(1000)
                .doOnComplete(() -> log.info("Call history request completed for user: {}", username))
                .doOnError(e -> log.error("Error retrieving call history: {}", e.getMessage()))
                .checkpoint("call-history-response");
    }
}
