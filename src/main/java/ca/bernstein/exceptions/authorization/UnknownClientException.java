package ca.bernstein.exceptions.authorization;

public class UnknownClientException extends AuthorizationException {

    public UnknownClientException(String message) {
        super(message);
    }

    public UnknownClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
