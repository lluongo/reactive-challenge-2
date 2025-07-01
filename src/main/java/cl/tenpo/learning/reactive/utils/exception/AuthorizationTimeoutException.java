package cl.tenpo.learning.reactive.utils.exception;

public class AuthorizationTimeoutException extends RuntimeException {
    public AuthorizationTimeoutException(String message) {
        super(message);
    }
}