package ca.bernstein.exceptions.web;

import ca.bernstein.models.authentication.LoginPageConfig;
import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;
import lombok.Getter;

import javax.ws.rs.core.Response;

public class LoginWebException extends AbstractWebApplicationException {

    @Getter private final LoginPageConfig loginPageConfig;

    public LoginWebException(Throwable cause, String... arguments) {
        this(cause, ErrorType.Authentication.SERVER_ERROR, arguments);
    }

    public LoginWebException(ErrorType.Authentication errorType, Response.Status status, LoginPageConfig loginPageConfig, String... arguments) {
        this(null, errorType, Response.Status.INTERNAL_SERVER_ERROR, loginPageConfig, arguments);
    }

    public LoginWebException(ErrorType.Authentication errorType, Response.Status status, String... arguments) {
        this(null, errorType, status, null, arguments);
    }

    public LoginWebException(Throwable cause, ErrorType.Authentication errorType, String... arguments) {
        this(cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, null, arguments);
    }

    public LoginWebException(Throwable cause, ErrorType.Authentication errorType, Response.Status status, LoginPageConfig loginPageConfig, String... arguments) {
        super(cause, new ErrorResponse(errorType, arguments), status);
        this.loginPageConfig = loginPageConfig;
    }

}
