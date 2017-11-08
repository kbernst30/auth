package ca.bernstein.models.error;

import lombok.Getter;

/**
 * Types that represent mapping of error type to appropriate error messages
 */
public final class ErrorType {

    public enum OAuth2 {
        GRANT_TYPE_NOT_ALLOWED("unauthorized_client", "Client [%s] is not authorized to request authorization of type [%s]"),
        MISSING_CLIENT_ID("invalid_request", "a valid client_id must be provided"),
        MISSING_REDIRECT_URI("invalid_request", "a valid redirect_uri must be provided"),
        NON_ABSOLUTE_REDIRECT_URI("invalid_request", "redirect_uri must be absolute"),
        INVALID_PARAMETER("invalid_request", "%s was invalid or otherwise malformed"),
        INVALID_SCOPE("invalid_scope", "Requested scope was invalid for client_id %s: %s"),
        REDIRECT_URI_HAS_FRAGMENT("invalid_request", "redirect_uri must not contain fragments"),
        SERVER_ERROR("server_error", "An unknown error occurred"),
        UNKNOWN_CLIENT_ID("invalid_request", "No client exists for given client_id: %s"),
        UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type", "response_type must be one of 'code' or 'token'");

        @Getter private final String error;
        @Getter private final String message;

        OAuth2(String error, String message) {
            this.error = error;
            this.message = message;
        }
    }
}
