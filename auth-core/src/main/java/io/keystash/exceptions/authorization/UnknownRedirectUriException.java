package io.keystash.exceptions.authorization;

import io.keystash.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class UnknownRedirectUriException extends OAuth2Exception {

    public UnknownRedirectUriException(String message) {
        super(message, ErrorType.OAuth2.UNKNOWN_REDIRECT_URI, Response.Status.BAD_REQUEST);
    }

    public UnknownRedirectUriException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.UNKNOWN_REDIRECT_URI, Response.Status.BAD_REQUEST);
    }

}
