package io.keystash.core.util;

import io.keystash.core.exceptions.LoginException;
import io.keystash.core.exceptions.OAuth2Exception;
import io.keystash.core.exceptions.OpenIdConnectException;
import io.keystash.core.models.authentication.LoginRequest;
import io.keystash.common.models.authentication.oidc.OidcAuthenticationRequest;
import io.keystash.common.models.authentication.oidc.OidcPrompt;
import io.keystash.common.models.authentication.oidc.OidcResponseType;
import io.keystash.common.models.common.AuthorizationRequest;
import io.keystash.common.models.common.AuthorizationResponseType;
import io.keystash.common.models.error.ErrorType;
import io.keystash.common.models.common.BasicAuthorizationDetails;
import io.keystash.common.models.oauth.OAuth2AuthorizationRequest;
import io.keystash.common.models.oauth.OAuth2GrantType;
import io.keystash.common.models.oauth.OAuth2ResponseType;
import io.keystash.common.models.oauth.OAuth2TokenRequest;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convenience methods for validations
 */
public final class Validations {

    /**
     * Validates the request object for an authorization request
     * @param authorizationRequest a valid AuthorizationRequest object
     */
    public static void validateAuthorizationRequest(AuthorizationRequest authorizationRequest) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = authorizationRequest.getOidcAuthenticationRequest();

        validateAuthorizationResponseTypes(authorizationRequest);
        validateOAuth2AuthorizationRequest(oAuth2AuthorizationRequest);

        if (authorizationRequest.isOpenIdConnectAuthRequest()) {
            validateOpenIdConnectAuthenticationRequest(oidcAuthenticationRequest);
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

    /**
     * Validate the authorization request response types against the other request params
     * @param authorizationRequest A valid AuthorizationRequest object
     */
    private static void validateAuthorizationResponseTypes(AuthorizationRequest authorizationRequest) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        Set<AuthorizationResponseType> responseTypes = oAuth2AuthorizationRequest != null ? oAuth2AuthorizationRequest.getResponseTypes() : new HashSet<>();

        boolean responseTypesIsEmpty = responseTypes.isEmpty();
        boolean isOauthMultipleResponseTypes = !authorizationRequest.isOpenIdConnectAuthRequest() && responseTypes.size() > 1;
        boolean isOpenIdTokenResponse = authorizationRequest.isOpenIdConnectAuthRequest() && responseTypes.size() == 1
                && responseTypes.contains(OAuth2ResponseType.TOKEN);
        boolean isOauthIdTokenResponse = !authorizationRequest.isOpenIdConnectAuthRequest()
                && responseTypes.contains(OidcResponseType.ID_TOKEN);

        if (responseTypesIsEmpty || isOauthMultipleResponseTypes || isOpenIdTokenResponse || isOauthIdTokenResponse) {
            throw new OAuth2Exception(ErrorType.OAuth2.UNSUPPORTED_RESPONSE_TYPE, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Validates the request object for an OAuth2.0 authorization request
     * @param oAuth2AuthorizationRequest A valid OAuth2AuthorizationRequest object
     */
    private static void validateOAuth2AuthorizationRequest(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
        validateRedirectUri(oAuth2AuthorizationRequest.getRedirectUri());

        if (StringUtils.isEmpty(oAuth2AuthorizationRequest.getClientId())) {
            throw new OAuth2Exception(ErrorType.OAuth2.MISSING_CLIENT_ID, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Validates the request object for an OpenID Connect authentication request
     * @param oidcAuthenticationRequest A valid OidcAuthenticationRequest object
     */
    private static void validateOpenIdConnectAuthenticationRequest(OidcAuthenticationRequest oidcAuthenticationRequest) {
        if (!StringUtils.isEmpty(oidcAuthenticationRequest.getPrompt())) {
            Set<OidcPrompt> prompts = Stream.of(oidcAuthenticationRequest.getPrompt().split(" "))
                    .map(OidcPrompt::fromString)
                    .collect(Collectors.toSet());

            if (prompts.contains(OidcPrompt.NONE) && prompts.size() > 1) {
                throw new OpenIdConnectException(ErrorType.OpenIdConnect.INVALID_NONE_PROMPT_USAGE, Response.Status.BAD_REQUEST);
            }
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
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_PARAMETER, Response.Status.BAD_REQUEST, "email");
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
