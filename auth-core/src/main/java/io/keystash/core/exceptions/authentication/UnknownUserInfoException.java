package io.keystash.core.exceptions.authentication;

import io.keystash.common.exceptions.OpenIdConnectException;
import io.keystash.common.models.error.ErrorType;

import javax.ws.rs.core.Response;

public class UnknownUserInfoException extends OpenIdConnectException {

    public UnknownUserInfoException(String message) {
        super(message, ErrorType.OpenIdConnect.UNKNOWN_USER_INFO, Response.Status.BAD_REQUEST);
    }

    public UnknownUserInfoException(String message, Throwable cause) {
        super(message, cause, ErrorType.OpenIdConnect.UNKNOWN_USER_INFO, Response.Status.BAD_REQUEST);
    }
}
