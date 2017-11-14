package ca.bernstein.util;

import ca.bernstein.exceptions.web.LoginWebException;
import ca.bernstein.exceptions.web.OAuth2WebException;
import ca.bernstein.models.authentication.LoginRequest;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.models.oauth.OAuth2AuthorizationRequest;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Convenience methods for validations
 */
public final class Validations {

    /**
     * Validates the request object for an OAuth2.0
     * @param oAuth2AuthorizationRequest A valid OAuth2AuthorizationRequest object
     */
    public static void validateOAuth2AuthorizationRequest(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
        if (oAuth2AuthorizationRequest.getResponseType() == null) {
            throw new OAuth2WebException(ErrorType.OAuth2.UNSUPPORTED_RESPONSE_TYPE, Response.Status.BAD_REQUEST);
        }

        validateRedirectUri(oAuth2AuthorizationRequest.getRedirectUri());

        if (StringUtils.isEmpty(oAuth2AuthorizationRequest.getClientId())) {
            throw new OAuth2WebException(ErrorType.OAuth2.MISSING_CLIENT_ID, Response.Status.BAD_REQUEST);
        }
    }

    public static void validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername()) || StringUtils.isEmpty(loginRequest.getPassword())) {
            throw new LoginWebException(ErrorType.Authentication.MISSING_CREDENTIALS, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Validates a redirect URI for OAuth2.0 requests
     * @param uri A string representing a URI
     */
    public static void validateRedirectUri(String uri) {
        if (StringUtils.isEmpty(uri)) {
            throw new OAuth2WebException(ErrorType.OAuth2.MISSING_REDIRECT_URI, Response.Status.BAD_REQUEST);
        }

        URI redirectUri;
        try {
            redirectUri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new OAuth2WebException(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "redirect_uri");
        }

        if (StringUtils.isEmpty(redirectUri.getScheme())) {
            throw new OAuth2WebException(ErrorType.OAuth2.NON_ABSOLUTE_REDIRECT_URI, Response.Status.BAD_REQUEST);
        }

        if (StringUtils.isNotEmpty(redirectUri.getFragment())) {
            throw new OAuth2WebException(ErrorType.OAuth2.REDIRECT_URI_HAS_FRAGMENT, Response.Status.BAD_REQUEST);
        }
    }
}
