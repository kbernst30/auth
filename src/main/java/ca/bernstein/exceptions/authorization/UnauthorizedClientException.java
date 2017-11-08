package ca.bernstein.exceptions.authorization;

public class UnauthorizedClientException extends AuthorizationException {

    public UnauthorizedClientException(String message) {
        super(message);
    }

    public UnauthorizedClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
