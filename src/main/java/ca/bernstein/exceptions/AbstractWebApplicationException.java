package ca.bernstein.exceptions;

import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;
import lombok.Getter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AbstractWebApplicationException extends WebApplicationException {

    @Getter private final ErrorResponse error;

    public AbstractWebApplicationException(Throwable cause, ErrorResponse error) {
        this(null, cause, error, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public AbstractWebApplicationException(String message, Throwable cause) {
        this(message, cause, null, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public AbstractWebApplicationException(String message, Throwable cause, ErrorResponse error) {
        this(message, cause, error, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public AbstractWebApplicationException(String message, Throwable cause, ErrorResponse error, Response.Status status) {
        super(message, cause, status);
        this.error = error;
    }

}
