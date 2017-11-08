package ca.bernstein.exceptions.jpa;

public class JpaInitializationException extends Exception {

    public JpaInitializationException() {
    }

    public JpaInitializationException(String message) {
        super(message);
    }

    public JpaInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JpaInitializationException(Throwable cause) {
        super(cause);
    }
}
