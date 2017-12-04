package io.keystash.exceptions.authorization;

import io.keystash.exceptions.OAuth2Exception;
import io.keystash.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidRefreshTokenException extends OAuth2Exception {

    public InvalidRefreshTokenException(String message) {
        super(message, ErrorType.OAuth2.INVALID_REFRESH_TOKEN, Response.Status.BAD_REQUEST);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.INVALID_REFRESH_TOKEN, Response.Status.BAD_REQUEST);
    }

}