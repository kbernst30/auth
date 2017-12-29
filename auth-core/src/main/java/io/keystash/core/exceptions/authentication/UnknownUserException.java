package io.keystash.core.exceptions.authentication;

public class UnknownUserException extends AuthenticationException {

    public UnknownUserException(String message) {
        super(message);
    }

    public UnknownUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
