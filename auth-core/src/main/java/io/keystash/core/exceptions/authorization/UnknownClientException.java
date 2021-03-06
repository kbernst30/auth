package io.keystash.core.exceptions.authorization;

import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class UnknownClientException extends OAuth2Exception {

    public UnknownClientException(String message, String clientId) {
        super(message, ErrorType.OAuth2.UNKNOWN_CLIENT_ID, Response.Status.BAD_REQUEST, clientId);
    }

    public UnknownClientException(String message, Throwable cause, String clientId) {
        super(message, cause, ErrorType.OAuth2.UNKNOWN_CLIENT_ID, Response.Status.BAD_REQUEST, clientId);
    }
}
