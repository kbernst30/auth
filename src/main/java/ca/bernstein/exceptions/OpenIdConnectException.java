package ca.bernstein.exceptions;

import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class OpenIdConnectException extends AbstractWebApplicationException {

    public OpenIdConnectException(Throwable cause, String... arguments) {
        this(cause, ErrorType.OpenIdConnect.SERVER_ERROR, arguments);
    }

    public OpenIdConnectException(String message, Throwable cause, String... arguments) {
        this(message, cause, ErrorType.OpenIdConnect.SERVER_ERROR, arguments);
    }

    public OpenIdConnectException(String message, ErrorType.OpenIdConnect errorType, String... arguments) {
        this(message, null, errorType, arguments);
    }

    public OpenIdConnectException(String message, Throwable cause, ErrorType.OpenIdConnect errorType, String... arguments) {
        this(message, cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public OpenIdConnectException(String message, ErrorType.OpenIdConnect errorType, Response.Status status, String... arguments) {
        this(message, null, errorType, status, arguments);
    }

    public OpenIdConnectException(ErrorType.OpenIdConnect errorType, Response.Status status, String... arguments) {
        this(null, null, errorType, status, arguments);
    }

    public OpenIdConnectException(Throwable cause, ErrorType.OpenIdConnect errorType, String... arguments) {
        this(null, cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, arguments);
    }

    public OpenIdConnectException(String message, Throwable cause, ErrorType.OpenIdConnect errorType, Response.Status status, String... arguments) {
        super(message, cause, new ErrorResponse(errorType, arguments), status);
    }
}
