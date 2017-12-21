package io.keystash.core.exceptions.authorization;

import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidScopeException extends OAuth2Exception {

    public InvalidScopeException(String message, String clientId, String scope) {
        super(message, ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST, clientId, scope);
    }

    public InvalidScopeException(String message, Throwable cause, String clientId, String scope) {
        super(message, cause, ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST, clientId, scope);
    }

    public InvalidScopeException(String message, String redirectUri, String clientId, String scope) {
        super(message, ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST, clientId, scope);
        setRedirectUri(redirectUri);
    }

    public InvalidScopeException(String message, String redirectUri, Throwable cause, String clientId, String scope) {
        super(message, cause, ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST, clientId, scope);
        setRedirectUri(redirectUri);
    }
}
