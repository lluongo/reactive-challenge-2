package cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
