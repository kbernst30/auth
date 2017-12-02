package io.keystash.exceptions;

import io.keystash.models.error.ErrorResponse;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AbstractWebApplicationException extends WebApplicationException {

    @Getter private final ErrorResponse error;
    @Getter @Setter private String redirectUri;

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
