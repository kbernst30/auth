package io.keystash.exceptions.authentication;

import io.keystash.exceptions.OpenIdConnectException;
import io.keystash.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class UnknownUserInfoException extends OpenIdConnectException {

    public UnknownUserInfoException(String message) {
        super(message, ErrorType.OpenIdConnect.UNKNOWN_USER_INFO, Response.Status.BAD_REQUEST);
    }

    public UnknownUserInfoException(String message, Throwable cause) {
        super(message, cause, ErrorType.OpenIdConnect.UNKNOWN_USER_INFO, Response.Status.BAD_REQUEST);
    }
}
