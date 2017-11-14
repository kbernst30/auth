package ca.bernstein.exceptions.web;

import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class LoginWebException extends AbstractWebApplicationException {

    public LoginWebException(Throwable cause, String... arguments) {
        this(cause, ErrorType.Authentication.SERVER_ERROR, arguments);
    }

    public LoginWebException(ErrorType.Authentication errorType, Response.Status status, String... arguments) {
        this(null, errorType, status, arguments);
    }

    public LoginWebException(Throwable cause, ErrorType.Authentication errorType, String... arguments) {
        this(cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public LoginWebException(Throwable cause, ErrorType.Authentication errorType, Response.Status status, String... arguments) {
        super(cause, new ErrorResponse(errorType, arguments), status);
    }

}
