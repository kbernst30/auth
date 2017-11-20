package ca.bernstein.exceptions.authorization;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class InvalidRefreshTokenException extends OAuth2Exception {

    public InvalidRefreshTokenException(String message) {
        super(message, ErrorType.OAuth2.INVALID_REFRESH_TOKEN, Response.Status.BAD_REQUEST);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.INVALID_REFRESH_TOKEN, Response.Status.BAD_REQUEST);
    }

}
