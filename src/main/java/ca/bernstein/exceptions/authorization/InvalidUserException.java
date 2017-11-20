package ca.bernstein.exceptions.authorization;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidUserException extends OAuth2Exception {

    public InvalidUserException(String message) {
        super(message, ErrorType.OAuth2.UNKNOWN_USER, Response.Status.FORBIDDEN);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.UNKNOWN_USER, Response.Status.FORBIDDEN);
    }
}
