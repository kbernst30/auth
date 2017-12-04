package io.keystash.exceptions.authorization;

import io.keystash.exceptions.OAuth2Exception;
import io.keystash.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidAuthorizationCodeException extends OAuth2Exception {

    public InvalidAuthorizationCodeException(String message, String code, String clientId) {
        super(message, ErrorType.OAuth2.INVALID_AUTHORIZATION_CODE, Response.Status.FORBIDDEN, code, clientId);
    }

    public InvalidAuthorizationCodeException(String message, Throwable cause, String code, String clientId) {
        super(message, cause, ErrorType.OAuth2.INVALID_AUTHORIZATION_CODE, Response.Status.FORBIDDEN, code, clientId);
    }
}