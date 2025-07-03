package cl.tenpo.learning.reactive.tasks.task2.application.port;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio para gestionar el historial de llamadas a la API.
 */
public interface CallHistoryService {
    
    /**
     * Obtiene el historial de llamadas a la API con paginación.
     * 
     * @param pageable configuración de paginación
     * @return Flux de registros de historial
     */
    Flux<CallHistory> getCallHistory(Pageable pageable);
    
    /**
     * Registra una solicitud exitosa en el historial.
     * 
     * @param endpoint endpoint de la solicitud
     * @param method método HTTP
     * @param parameters parámetros de la solicitud
     * @param response respuesta obtenida
     * @return Mono con el registro creado
     */
    Mono<CallHistory> recordSuccessfulRequest(String endpoint, String method, String parameters, String response);
    
    /**
     * Registra una solicitud fallida en el historial.
     * 
     * @param endpoint endpoint de la solicitud
     * @param method método HTTP
     * @param parameters parámetros de la solicitud
     * @param error mensaje de error
     * @return Mono con el registro creado
     */
    Mono<CallHistory> recordFailedRequest(String endpoint, String method, String parameters, String error);
}
