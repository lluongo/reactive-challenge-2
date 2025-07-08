package cl.tenpo.learning.reactive.tasks.task2.infrastructure.config;

import cl.tenpo.learning.reactive.tasks.task2.presentation.handler.CalculationHandler;
import cl.tenpo.learning.reactive.tasks.task2.presentation.handler.HistoryHandler;
import cl.tenpo.learning.reactive.tasks.task2.presentation.handler.UserHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RouterConfig {
    
    @Value("${app.api.endpoints.calculation}")
    private String calculationEndpoint;
    
    @Value("${app.api.endpoints.history}")
    private String historyEndpoint;
    
    @Value("${app.api.endpoints.users}")
    private String usersEndpoint;
    
    @Value("${app.api.endpoints.ping}")
    private String pingEndpoint;
    
    @Value("${app.api.base-paths.functional}")
    private String functionalBasePath;
    
    @Value("${app.api.base-paths.alternative}")
    private String alternativeBasePath;
    
    @Bean
    public RouterFunction<ServerResponse> functionalRoutes(
            CalculationHandler calculationHandler,
            HistoryHandler historyHandler,
            UserHandler userHandler) {
        return RouterFunctions.route()
                .path(functionalBasePath, builder -> builder
                        .GET(pingEndpoint, request -> ServerResponse.ok().bodyValue("Functional routes are working!"))
                        .POST(calculationEndpoint, calculationHandler::calculate)
                        .GET(historyEndpoint, historyHandler::getHistory)
                        .GET(usersEndpoint, userHandler::getAllUsers)
                        .POST(usersEndpoint, userHandler::createUser)
                        .GET(usersEndpoint + "/{id}", userHandler::getUserById)
                        .DELETE(usersEndpoint + "/{id}", userHandler::deactivateUser)
                )
                .build();
    }
    
    @Bean
    public RouterFunction<ServerResponse> alternativeRoutes(
            CalculationHandler calculationHandler) {
        return RouterFunctions.route()
                .path(alternativeBasePath, builder -> builder
                        .POST("/calc", accept(MediaType.APPLICATION_JSON), calculationHandler::calculate)
                )
                .build();
    }
}
