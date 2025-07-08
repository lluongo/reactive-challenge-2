package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.PercentageService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Servicio para obtener el porcentaje desde una API externa con fallback a caché
 * Requisito: Se debe verificar siempre primero el servicio externo
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalPercentageService implements PercentageService {

    private final ExternalApiClient externalApiClient;
    private final PercentageCacheService cacheService;

    @Override
    public Mono<BigDecimal> getPercentage() {
        log.info("🚀🚀🚀 GETPERCENTAGE() CALLED - INTENTANDO OBTENER DEL SERVICIO EXTERNO PRIMERO 🚀🚀🚀");
        
        return externalApiClient.fetchPercentage()
                .flatMap(percentage -> {
                    log.info("✅✅✅ SERVICIO EXTERNO EXITOSO - ALMACENANDO EN CACHE: {} ✅✅✅", percentage);
                    return cacheService.cachePercentage(percentage)
                            .thenReturn(percentage);
                })
                .onErrorResume(error -> {
                    log.error("🔴🔴🔴 ERROR EN SERVICIO EXTERNO: {} - INTENTANDO USAR VALOR EN CACHÉ 🔴🔴🔴", error.getMessage());
                    return cacheService.getCachedPercentage()
                            .doOnNext(cached -> log.info("✅✅✅ USANDO VALOR EN CACHÉ COMO FALLBACK: {} ✅✅✅", cached))
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("💥💥💥 NO HAY VALOR EN CACHÉ DISPONIBLE - RESPONDIENDO CON ERROR 💥💥💥");
                                return Mono.error(new ServiceUnavailableException("Servicio externo no disponible y no hay valor en caché"));
                            }));
                })
                .doOnSubscribe(s -> log.info("🔔🔔🔔 PERCENTAGE SERVICE SUBSCRIBED 🔔🔔🔔"))
                .doOnSuccess(val -> log.info("🎉🎉🎉 PERCENTAGE SERVICE SUCCESS: {} 🎉🎉🎉", val))
                .doOnError(err -> log.error("💥💥💥 PERCENTAGE SERVICE ERROR: {} 💥💥💥", err.getMessage()));
    }
}
