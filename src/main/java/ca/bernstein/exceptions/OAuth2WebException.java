package ca.bernstein.exceptions;

import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class OAuth2WebException extends WebApplicationException {

    private ErrorResponse error;

    public OAuth2WebException(Throwable cause, String... arguments) {
        this(cause, ErrorType.OAuth2.SERVER_ERROR, arguments);
    }

    public OAuth2WebException(Throwable cause, ErrorResponse error) {
        this(cause, error, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public OAuth2WebException(Throwable cause, ErrorResponse error, Response.Status status) {
        super(cause, status);
        this.error = error;
    }

    public OAuth2WebException(ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        this(null, errorType, status, arguments);
    }

    public OAuth2WebException(Throwable cause, ErrorType.OAuth2 errorType, String... arguments) {
        this(cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public OAuth2WebException(Throwable cause, ErrorType.OAuth2 errorType, Response.Status status, String... arguments) {
        super(cause, status);
        this.error = new ErrorResponse(errorType, arguments);
    }

    public ErrorResponse getError() {
        return error;
    }
}
