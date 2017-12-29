package io.keystash.core.resources.authorization;

import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.exceptions.OpenIdConnectException;
import io.keystash.core.exceptions.authorization.AuthorizationException;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.authentication.oidc.OidcAuthenticationRequest;
import io.keystash.common.models.authentication.oidc.OidcPrompt;
import io.keystash.common.models.authentication.oidc.OidcResponseType;
import io.keystash.common.models.common.AuthorizationRequest;
import io.keystash.common.models.common.AuthorizationResponseType;
import io.keystash.common.models.common.BasicAuthorizationDetails;
import io.keystash.common.models.error.ErrorType;
import io.keystash.core.services.authorization.AuthorizationService;
import io.keystash.core.util.AuthenticationUtils;
import io.keystash.common.util.AuthorizationUtils;
import io.keystash.core.util.Validations;
import io.keystash.common.models.oauth.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

/**
 * A resource for OAuth2.0 authorization requests
 */
@Slf4j
@Path("/oauth")
public class AuthorizationResource {

    private final AuthorizationService authorizationService;

    @Inject
    public AuthorizationResource(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Processes an OAuth2.0 authorize request to the /authorize endpoint
     * <p>A valid session is necessary to get a successful response from this resource.</p>
     * <p>
     *     To request OpenID Connect authentication, <i>openid</i> must be specified as a requested scope
     * </p>
     *
     * @param authorizationRequest A valid AuthorizationRequest object
     * @param session An active session instance
     * @return A valid Response redirect to a valid specified redirect URI
     */
    @GET
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOAuth2Authorization(@BeanParam AuthorizationRequest authorizationRequest,
                                           @Context HttpSession session, @Context UriInfo uriInfo) {

        // Validate request parameters
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = authorizationRequest.getOidcAuthenticationRequest();

        Validations.validateAuthorizationRequest(authorizationRequest);

        Set<AuthorizationResponseType> authorizationResponseTypes = oAuth2AuthorizationRequest != null ? oAuth2AuthorizationRequest.getResponseTypes() : new HashSet<>();
        boolean isOpenIdConnectLoginPrompt = authorizationRequest.isOpenIdConnectAuthRequest()
                && OidcPrompt.LOGIN == OidcPrompt.fromString(oidcAuthenticationRequest.getPrompt());
        boolean isOpenIdConnectNonePrompt = authorizationRequest.isOpenIdConnectAuthRequest()
                && OidcPrompt.NONE == OidcPrompt.fromString(oidcAuthenticationRequest.getPrompt());

        // If session is invalid OR we've asked for re-authentication, abort this request and re-authenticate
        // Unless this is an OpenID Connect request with no prompt, in that case throw an error
        if (!AuthenticationUtils.isValidSession(session) && isOpenIdConnectNonePrompt) {
            throw new OpenIdConnectException(ErrorType.OpenIdConnect.LOGIN_REQUIRED, Response.Status.UNAUTHORIZED);
        }

        if (!AuthenticationUtils.isValidSession(session) || isOpenIdConnectLoginPrompt) {
            session.invalidate();
            return abortWithAuthenticationRequest(uriInfo.getAbsolutePath().toString(), uriInfo.getQueryParameters());
        }

        AuthenticatedUser authenticatedUser = AuthenticationUtils.getUserFromSession(session);

        if (authorizationResponseTypes.contains(OAuth2ResponseType.CODE)) {
            return getOAuth2AuthorizationCodeResponse(authorizationRequest, authenticatedUser, session, uriInfo.getBaseUri());
        } else {
            return getOauth2AuthorizationTokenResponse(authorizationRequest, authenticatedUser, session, uriInfo.getBaseUri());
        }
    }

    /**
     * Processes an OAuth2.0 token request to the /token endpoint
     * @param oAuth2TokenRequest A valid OAuth2TokenRequest object
     * @return A valid Response containing a token response object in JSON format
     */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOAuth2Token(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                   @BeanParam OAuth2TokenRequest oAuth2TokenRequest, @Context UriInfo uriInfo) {

        BasicAuthorizationDetails authorizationDetails = BasicAuthorizationDetails.fromHeaderString(authorizationHeader);
        Validations.validateOAuth2TokenRequest(oAuth2TokenRequest, authorizationDetails);

        OAuth2TokenResponse tokenResponse = null;
        Set<String> requestedScopes = AuthorizationUtils.getScopes(oAuth2TokenRequest.getScope());

        if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.AUTHORIZATION_CODE) {
            tokenResponse = authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2TokenRequest.getCode(),
                    authorizationDetails, oAuth2TokenRequest.getRedirectUri());

        } else if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.CLIENT_CREDENTIALS) {
            tokenResponse = authorizationService.getTokenResponseForClientCredentialsGrant(authorizationDetails, requestedScopes);

        } else if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.PASSWORD) {
            tokenResponse = authorizationService.getTokenResponseForPasswordGrant(authorizationDetails,
                    oAuth2TokenRequest.getUsername(), oAuth2TokenRequest.getPassword(), requestedScopes, uriInfo.getBaseUri().getHost());

        } else if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.REFRESH_TOKEN) {
            tokenResponse = authorizationService.getTokenResponseForRefreshTokenGrant(authorizationDetails, oAuth2TokenRequest.getRefreshToken());
        }

        // We should never get here as validations would've ensured response type value
        if (tokenResponse == null) {
            throw new OAuth2Exception(ErrorType.OAuth2.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok(tokenResponse).build();
    }

    private Response getOAuth2AuthorizationCodeResponse(AuthorizationRequest authorizationRequest, AuthenticatedUser authenticatedUser,
                                                        HttpSession session, URI baseUri) throws AuthorizationException {

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OAuth2AuthCode authCode = authorizationService.generateAuthorizationCode(authorizationRequest, authenticatedUser);

        boolean didRequestToken = oAuth2AuthorizationRequest.getResponseTypes().contains(OAuth2ResponseType.TOKEN);
        boolean didRequestIdToken = oAuth2AuthorizationRequest.getResponseTypes().contains(OidcResponseType.ID_TOKEN);
        boolean isHybridRequest = authorizationRequest.isOpenIdConnectAuthRequest() && (didRequestToken || didRequestIdToken);

        String sessionState = null;
        if (!StringUtils.isEmpty(session.getId())) {
            sessionState = AuthenticationUtils.getSessionState(oAuth2AuthorizationRequest.getClientId(), baseUri.toString(), session.getId());
        }

        URI requestedUri = URI.create(oAuth2AuthorizationRequest.getRedirectUri());
        UriBuilder resolvedUriBuilder = UriBuilder.fromUri(requestedUri);

        if (isHybridRequest) {

            StringBuilder fragment = new StringBuilder();
            fragment.append("code=").append(authCode.getCode());

            if (didRequestToken) {
                fragment.append("&access_token=").append(authCode.getAccessToken());
                fragment.append("&token_type=").append("bearer");
                fragment.append("&scope=").append(String.join(" ", authCode.getResolvedScopes()));
            }

            if (didRequestIdToken) {
                fragment.append("&id_token=").append(authCode.getIdToken());
            }

            if (!StringUtils.isEmpty(oAuth2AuthorizationRequest.getState())) {
                fragment.append("&state=").append(oAuth2AuthorizationRequest.getState());
            }

            if (!StringUtils.isEmpty(sessionState)) {
                fragment.append("&session_state=").append(sessionState);
            }

            resolvedUriBuilder.fragment(fragment.toString());
        } else {
            resolvedUriBuilder.queryParam("code", authCode.getCode());
            if (!StringUtils.isEmpty(oAuth2AuthorizationRequest.getState())) {
                resolvedUriBuilder.queryParam("state", oAuth2AuthorizationRequest.getState());
            }

            if (!StringUtils.isEmpty(sessionState)) {
                resolvedUriBuilder.queryParam("session_state", sessionState);
            }
        }

        URI resolvedRedirectUri = resolvedUriBuilder.build();

        return Response.temporaryRedirect(resolvedRedirectUri).build();
    }

    private Response getOauth2AuthorizationTokenResponse(AuthorizationRequest authorizationRequest, AuthenticatedUser authenticatedUser,
                                                         HttpSession session, URI baseUri) throws AuthorizationException {

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForImplicitGrant(authorizationRequest,
                authenticatedUser);

        String responseFragment = oAuth2TokenResponse.getAsUrlEncodedFormParams();
        if (!StringUtils.isEmpty(session.getId())) {
            try {
                String sessionState = AuthenticationUtils.getSessionState(oAuth2AuthorizationRequest.getClientId(), baseUri.toString(), session.getId());
                responseFragment = responseFragment + "&session_state=" + (new URLCodec()).encode(sessionState);
            } catch (EncoderException e) {
                log.warn("Unable to encode sessionState for response fragment", e);
            }
        }

        URI requestedUri = URI.create(oAuth2AuthorizationRequest.getRedirectUri());
        URI resolvedRedirectUri = UriBuilder.fromUri(requestedUri)
                .fragment(responseFragment)
                .build();

        return Response.temporaryRedirect(resolvedRedirectUri).build();
    }

    private Response abortWithAuthenticationRequest(String requestedUri, Map<String, List<String>> queryParameters) {
        Map<String, List<String>> resolvedQueryParams = new HashMap<>(queryParameters);
        if (resolvedQueryParams.containsKey("prompt")) {
            resolvedQueryParams.remove("prompt");
        }

        UriBuilder resolvedUriBuilder = UriBuilder.fromUri(requestedUri);
        resolvedQueryParams.forEach((key, value) -> value.forEach(param -> resolvedUriBuilder.queryParam(key, param)));

        URI authenticationUri;
        try {
            authenticationUri = URI.create("/auth/login?returnTo=" + (new URLCodec()).encode(resolvedUriBuilder.build().toString()));
        } catch (EncoderException e) {
            log.warn("Failed to encode returnTo URI [{}]", requestedUri);
            authenticationUri = URI.create("/auth/login");
        }

        return Response.seeOther(authenticationUri).build();
    }
}
