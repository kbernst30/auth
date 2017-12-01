package ca.bernstein.exceptions.authorization;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.error.ErrorType;

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
