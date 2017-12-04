package io.keystash.common.exceptions;

public class SigningKeyException extends Exception {

    public SigningKeyException(String message) {
        super(message);
    }

    public SigningKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
