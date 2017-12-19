package io.keystash.core.exceptions.authentication;

public class UnknownAccountException extends AuthenticationException {

    public UnknownAccountException(String message) {
        super(message);
    }

    public UnknownAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
