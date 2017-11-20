package ca.bernstein.exceptions.authorization;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidClientException extends OAuth2Exception {

    public InvalidClientException(String message) {
        super(message, ErrorType.OAuth2.INVALID_CLIENT, Response.Status.UNAUTHORIZED);
    }

    public InvalidClientException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.INVALID_CLIENT, Response.Status.UNAUTHORIZED);
    }
}
