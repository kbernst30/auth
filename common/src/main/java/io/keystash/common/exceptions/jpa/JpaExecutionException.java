package io.keystash.common.exceptions.jpa;

public class JpaExecutionException extends Exception {

    public JpaExecutionException() {
    }

    public JpaExecutionException(String message) {
        super(message);
    }

    public JpaExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JpaExecutionException(Throwable cause) {
        super(cause);
    }
}
