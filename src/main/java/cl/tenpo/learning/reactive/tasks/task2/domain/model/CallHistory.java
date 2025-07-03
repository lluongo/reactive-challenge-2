package cl.tenpo.learning.reactive.tasks.task2.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "call_history")
public class CallHistory {

    @Id
    private String id;
    private LocalDateTime timestamp;
    private String endpoint;
    private String method;
    private String parameters;
    private String response;
    private String error;
    private boolean successful;
    public static CallHistory createSuccessfulRecord(String endpoint, String method, String parameters, String response) {
        return CallHistory.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .parameters(parameters)
                .response(response)
                .successful(true)
                .build();
    }
    public static CallHistory createFailedRecord(String endpoint, String method, String parameters, String error) {
        return CallHistory.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .parameters(parameters)
                .error(error)
                .successful(false)
                .build();
    }
}
