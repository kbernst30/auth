package io.keystash.core.exceptions.authorization;

import io.keystash.core.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;

public class AuthorizationException extends OAuth2Exception {

    public AuthorizationException(String message) {
        super(message, ErrorType.OAuth2.SERVER_ERROR);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.SERVER_ERROR);
    }

    public AuthorizationException(String message, String redirectUri) {
        super(message, ErrorType.OAuth2.SERVER_ERROR);
        this.setRedirectUri(redirectUri);
    }

    public AuthorizationException(String message, String redirectUri, Throwable cause) {
        super(message, cause, ErrorType.OAuth2.SERVER_ERROR);
        this.setRedirectUri(redirectUri);
    }
}
