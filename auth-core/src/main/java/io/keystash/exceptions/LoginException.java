package io.keystash.exceptions;

import io.keystash.models.authentication.LoginPageConfig;
import io.keystash.models.error.ErrorResponse;
import io.keystash.models.error.ErrorType;
import lombok.Getter;

import javax.ws.rs.core.Response;

public class LoginException extends AbstractWebApplicationException {

    @Getter private final LoginPageConfig loginPageConfig;

    public LoginException(Throwable cause, String... arguments) {
        this(cause, ErrorType.Authentication.SERVER_ERROR, arguments);
    }

    public LoginException(ErrorType.Authentication errorType, Response.Status status, LoginPageConfig loginPageConfig, String... arguments) {
        this(null, errorType, Response.Status.INTERNAL_SERVER_ERROR, loginPageConfig, arguments);
    }

    public LoginException(ErrorType.Authentication errorType, Response.Status status, String... arguments) {
        this(null, errorType, status, null, arguments);
    }

    public LoginException(Throwable cause, ErrorType.Authentication errorType, String... arguments) {
        this(cause, errorType, Response.Status.INTERNAL_SERVER_ERROR, null, arguments);
    }

    public LoginException(Throwable cause, ErrorType.Authentication errorType, Response.Status status, LoginPageConfig loginPageConfig, String... arguments) {
        super(null, cause, new ErrorResponse(errorType, arguments), status);
        this.loginPageConfig = loginPageConfig;
    }

}
