package ca.bernstein.util;

import ca.bernstein.exceptions.LoginException;
import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.authentication.LoginRequest;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.models.oauth.BasicAuthorizationDetails;
import ca.bernstein.models.oauth.OAuth2AuthorizationRequest;
import ca.bernstein.models.oauth.OAuth2GrantType;
import ca.bernstein.models.oauth.OAuth2TokenRequest;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Convenience methods for validations
 */
public final class Validations {

    /**
     * Validates the request object for an OAuth2.0 authorization request
     * @param oAuth2AuthorizationRequest A valid OAuth2AuthorizationRequest object
     */
    public static void validateOAuth2AuthorizationRequest(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
        if (oAuth2AuthorizationRequest.getResponseType() == null) {
            throw new OAuth2Exception(ErrorType.OAuth2.UNSUPPORTED_RESPONSE_TYPE, Response.Status.BAD_REQUEST);
        }

        validateRedirectUri(oAuth2AuthorizationRequest.getRedirectUri());

        if (StringUtils.isEmpty(oAuth2AuthorizationRequest.getClientId())) {
            throw new OAuth2Exception(ErrorType.OAuth2.MISSING_CLIENT_ID, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Validates the request object for an OAuth2.0 token request
     * @param oAuth2TokenRequest A valid OAuth2TokenRequest object
     */
    public static void validateOAuth2TokenRequest(OAuth2TokenRequest oAuth2TokenRequest, BasicAuthorizationDetails authorizationDetails) {
        OAuth2GrantType grantType = oAuth2TokenRequest.getGrantType();
        switch (grantType) {
            case AUTHORIZATION_CODE:
                validateAuthorizationCodeGrant(oAuth2TokenRequest);
                break;
            case CLIENT_CREDENTIALS:
                // do nothing
                break;
            case IMPLICIT:
                throw new OAuth2Exception(ErrorType.OAuth2.INVALID_IMPLICIT_GRANT, Response.Status.BAD_REQUEST);
            case PASSWORD:
                validatePasswordGrant(oAuth2TokenRequest);
                break;
            case REFRESH_TOKEN:
                validateRefreshTokenGrant(oAuth2TokenRequest);
                break;
            default:
                throw new OAuth2Exception(ErrorType.OAuth2.INVALID_GRANT, Response.Status.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(authorizationDetails.getClientId()) || StringUtils.isEmpty(authorizationDetails.getClientSecret())) {
            throw new OAuth2Exception(ErrorType.OAuth2.MISSING_CLIENT_ID, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Validates a login request
     * @param loginRequest A valid LoginRequest object
     */
    public static void validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername()) || StringUtils.isEmpty(loginRequest.getPassword())) {
            throw new LoginException(ErrorType.Authentication.MISSING_CREDENTIALS, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Validates a redirect URI for OAuth2.0 requests
     * @param uri A string representing a URI
     */
    public static void validateRedirectUri(String uri) {
        if (StringUtils.isEmpty(uri)) {
            throw new OAuth2Exception(ErrorType.OAuth2.MISSING_REDIRECT_URI, Response.Status.BAD_REQUEST);
        }

        URI redirectUri;
        try {
            redirectUri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "redirect_uri");
        }

        if (StringUtils.isEmpty(redirectUri.getScheme())) {
            throw new OAuth2Exception(ErrorType.OAuth2.NON_ABSOLUTE_REDIRECT_URI, Response.Status.BAD_REQUEST);
        }

        if (StringUtils.isNotEmpty(redirectUri.getFragment())) {
            throw new OAuth2Exception(ErrorType.OAuth2.REDIRECT_URI_HAS_FRAGMENT, Response.Status.BAD_REQUEST);
        }
    }

    private static void validateAuthorizationCodeGrant(OAuth2TokenRequest oAuth2TokenRequest) {
        validateRedirectUri(oAuth2TokenRequest.getRedirectUri());

        if (StringUtils.isEmpty(oAuth2TokenRequest.getCode())) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "code");
        }
    }

    private static void validatePasswordGrant(OAuth2TokenRequest oAuth2TokenRequest) {
        if (StringUtils.isEmpty(oAuth2TokenRequest.getUsername())) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "username");
        }

        if (StringUtils.isEmpty(oAuth2TokenRequest.getPassword())) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "password");
        }
    }

    private static void validateRefreshTokenGrant(OAuth2TokenRequest oAuth2TokenRequest) {
        if (StringUtils.isEmpty(oAuth2TokenRequest.getRefreshToken())) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "refresh_token");
        }
    }
}
