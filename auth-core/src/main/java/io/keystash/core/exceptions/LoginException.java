package io.keystash.core.exceptions;

import io.keystash.common.exceptions.AbstractWebApplicationException;
import io.keystash.core.models.authentication.LoginPageConfig;
import io.keystash.common.models.error.ErrorResponse;
import io.keystash.common.models.error.ErrorType;
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
