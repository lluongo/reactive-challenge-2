package cl.tenpo.learning.reactive.tasks.task2.infrastructure.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.Map;

@Getter
public class RetryExhaustedEvent extends ApplicationEvent {

    private final Map<String, String> errorData;
    public RetryExhaustedEvent(Map<String, String> errorData) {
        super(errorData);
        this.errorData = errorData;
    }
    
    public RetryExhaustedEvent(String errorMessage) {
        super(errorMessage);
        this.errorData = Map.of("error", errorMessage);
    }
}
