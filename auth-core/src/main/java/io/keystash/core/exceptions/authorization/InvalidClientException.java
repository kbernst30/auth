package io.keystash.core.exceptions.authorization;

import io.keystash.core.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidClientException extends OAuth2Exception {

    public InvalidClientException(String message) {
        super(message, ErrorType.OAuth2.INVALID_CLIENT, Response.Status.UNAUTHORIZED);
    }

    public InvalidClientException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.INVALID_CLIENT, Response.Status.UNAUTHORIZED);
    }
}
