package ca.bernstein.exceptions.web;

import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class OAuth2WebException extends AbstractWebApplicationException {

    public OAuth2WebException(Throwable cause, String... arguments) {
        this(cause, ErrorType.OAuth2.SERVER_ERROR, arguments);
    }

    public OAuth2WebException(ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        this(null, errorType, status, arguments);
    }

    public OAuth2WebException(Throwable cause, ErrorType.OAuth2 errorType, String... arguments) {
        this(cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public OAuth2WebException(Throwable cause, ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        super(cause, new ErrorResponse(errorType, arguments), status);
    }
}
