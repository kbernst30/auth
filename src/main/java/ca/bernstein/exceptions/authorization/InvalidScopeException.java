package ca.bernstein.exceptions.authorization;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidScopeException extends OAuth2Exception {

    public InvalidScopeException(String message, String clientId, String scope) {
        super(message, ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST, clientId, scope);
    }

    public InvalidScopeException(String message, Throwable cause, String clientId, String scope) {
        super(message, cause, ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST, clientId, scope);
    }
}
