package ca.bernstein.exceptions.authorization;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class UnknownClientException extends OAuth2Exception {

    public UnknownClientException(String message, String clientId) {
        super(message, ErrorType.OAuth2.UNKNOWN_CLIENT_ID, Response.Status.BAD_REQUEST, clientId);
    }

    public UnknownClientException(String message, Throwable cause, String clientId) {
        super(message, cause, ErrorType.OAuth2.UNKNOWN_CLIENT_ID, Response.Status.BAD_REQUEST, clientId);
    }
}
