package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import cl.tenpo.learning.reactive.tasks.task2.presentation.handler.CalculationHandler;
import cl.tenpo.learning.reactive.tasks.task2.presentation.handler.HistoryHandler;
import cl.tenpo.learning.reactive.tasks.task2.presentation.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Configuración de rutas funcionales usando Router Functions.
 * 
 * Esta configuración proporciona una alternativa funcional a los controllers tradicionales,
 * cumpliendo con el bonus del challenge de implementar Routing Functions.
 */
@Configuration
public class RouterConfig {

    /**
     * Define las rutas funcionales para la API.
     * 
     * Rutas disponibles:
     * - POST /functional/calculation - Cálculo con porcentaje
     * - GET /functional/history - Historial de llamadas  
     * - GET /functional/users - Obtener usuarios
     * - POST /functional/users - Crear usuario
     * - GET /functional/users/{id} - Obtener usuario por ID
     * - DELETE /functional/users/{id} - Desactivar usuario
     */
    @Bean
    public RouterFunction<ServerResponse> functionalRoutes(
            CalculationHandler calculationHandler,
            HistoryHandler historyHandler,
            UserHandler userHandler) {

        return route()
                // Rutas de cálculo
                .POST("/functional/calculation", 
                      accept(MediaType.APPLICATION_JSON), 
                      calculationHandler::calculate)
                
                // Rutas de historial
                .GET("/functional/history", 
                     accept(MediaType.APPLICATION_JSON), 
                     historyHandler::getHistory)
                
                // Rutas de usuarios
                .GET("/functional/users", 
                     accept(MediaType.APPLICATION_JSON), 
                     userHandler::getAllUsers)
                
                .POST("/functional/users", 
                      accept(MediaType.APPLICATION_JSON), 
                      userHandler::createUser)
                
                .GET("/functional/users/{id}", 
                     accept(MediaType.APPLICATION_JSON), 
                     userHandler::getUserById)
                
                .DELETE("/functional/users/{id}", 
                        accept(MediaType.APPLICATION_JSON), 
                        userHandler::deactivateUser)
                
                .build();
    }

    /**
     * Router alternativo con diferentes paths para demostrar flexibilidad.
     */
    @Bean
    public RouterFunction<ServerResponse> alternativeRoutes(
            CalculationHandler calculationHandler) {

        return route()
                .GET("/functional/ping", 
                     request -> ServerResponse.ok().bodyValue("Functional routes are working!"))
                
                .POST("/functional/calc", 
                      accept(MediaType.APPLICATION_JSON), 
                      calculationHandler::calculate)
                
                .build();
    }
}
