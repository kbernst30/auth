package io.keystash.exceptions.authorization;

public class SigningKeyException extends Exception {

    public SigningKeyException(String message) {
        super(message);
    }

    public SigningKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
