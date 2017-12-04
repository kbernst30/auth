package io.keystash.exceptions.authorization;

import io.keystash.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class UnauthorizedClientException extends OAuth2Exception {

    public UnauthorizedClientException(String message, String clientId, String grantType) {
        super(message, ErrorType.OAuth2.GRANT_TYPE_NOT_ALLOWED, Response.Status.BAD_REQUEST, clientId, grantType);
    }

    public UnauthorizedClientException(String message, Throwable cause, String clientId, String grantType) {
        super(message, cause, ErrorType.OAuth2.GRANT_TYPE_NOT_ALLOWED, Response.Status.BAD_REQUEST, clientId, grantType);
    }
}
