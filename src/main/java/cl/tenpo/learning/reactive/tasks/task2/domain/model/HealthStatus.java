package cl.tenpo.learning.reactive.tasks.task2.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad para comprobar el estado de la aplicación y la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "health")
public class HealthStatus {
    
    @Id
    private Long id;
    private String status;
}
