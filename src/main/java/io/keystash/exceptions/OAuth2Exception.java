package io.keystash.exceptions;

import io.keystash.models.error.ErrorResponse;
import io.keystash.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class OAuth2Exception extends AbstractWebApplicationException {

    public OAuth2Exception(Throwable cause, String... arguments) {
        this(cause, ErrorType.OAuth2.SERVER_ERROR, arguments);
    }

    public OAuth2Exception(String message, Throwable cause, String... arguments) {
        this(message, cause, ErrorType.OAuth2.SERVER_ERROR, arguments);
    }

    public OAuth2Exception(String message, ErrorType.OAuth2 errorType, String... arguments) {
        this(message, null, errorType, arguments);
    }

    public OAuth2Exception(String message, Throwable cause, ErrorType.OAuth2 errorType, String... arguments) {
        this(message, cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public OAuth2Exception(String message, ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        this(message, null, errorType, status, arguments);
    }

    public OAuth2Exception(ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        this(null, null, errorType, status, arguments);
    }

    public OAuth2Exception(Throwable cause, ErrorType.OAuth2 errorType, String... arguments) {
        this(null, cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public OAuth2Exception(String message, Throwable cause, ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        super(message, cause, new ErrorResponse(errorType, arguments), status);
    }
}
