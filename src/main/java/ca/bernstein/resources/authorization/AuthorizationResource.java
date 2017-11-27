package ca.bernstein.resources.authorization;

import ca.bernstein.annotation.AuthenticationRequired;
import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.exceptions.OpenIdConnectException;
import ca.bernstein.exceptions.authorization.AuthorizationException;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.authentication.oidc.OidcAuthenticationRequest;
import ca.bernstein.models.authentication.oidc.OidcPrompt;
import ca.bernstein.models.common.AuthorizationRequest;
import ca.bernstein.models.common.BasicAuthorizationDetails;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.models.oauth.*;
import ca.bernstein.services.authorization.OAuth2AuthorizationService;
import ca.bernstein.util.AuthenticationUtils;
import ca.bernstein.util.Validations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.ietf.jgss.Oid;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A resource for OAuth2.0 authorization requests
 */
@Slf4j
@Path("/oauth")
public class AuthorizationResource {

    private final OAuth2AuthorizationService authorizationService;

    @Inject
    public AuthorizationResource(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Processes an OAuth2.0 authorize request to the /authorize endpoint
     * <p>A valid session is necessary to get a successful response from this resource.</p>
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
        Validations.validateAuthorizationRequest(authorizationRequest);

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = authorizationRequest.getOidcAuthenticationRequest();
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

        if (oAuth2AuthorizationRequest.getResponseType() == OAuth2ResponseType.CODE) {
            return getOAuth2AuthorizationCodeResponse(oAuth2AuthorizationRequest, authenticatedUser);
        } else if (oAuth2AuthorizationRequest.getResponseType() == OAuth2ResponseType.TOKEN) {
            return getOauth2AuthorizationTokenResponse(oAuth2AuthorizationRequest, authenticatedUser);
        }

        throw new OAuth2Exception(ErrorType.OAuth2.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
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
                                   @BeanParam OAuth2TokenRequest oAuth2TokenRequest) {

        BasicAuthorizationDetails authorizationDetails = BasicAuthorizationDetails.fromHeaderString(authorizationHeader);
        Validations.validateOAuth2TokenRequest(oAuth2TokenRequest, authorizationDetails);

        OAuth2TokenResponse tokenResponse = null;
        Set<String> requestedScopes = getRequestedScopes(oAuth2TokenRequest.getScope());

        if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.AUTHORIZATION_CODE) {
            tokenResponse = authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2TokenRequest.getCode(),
                    authorizationDetails, oAuth2TokenRequest.getRedirectUri());

        } else if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.CLIENT_CREDENTIALS) {
            tokenResponse = authorizationService.getTokenResponseForClientCredentialsGrant(authorizationDetails, requestedScopes);

        } else if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.PASSWORD) {
            tokenResponse = authorizationService.getTokenResponseForPasswordGrant(authorizationDetails,
                    oAuth2TokenRequest.getUsername(), oAuth2TokenRequest.getPassword(), requestedScopes);

        } else if (oAuth2TokenRequest.getGrantType() == OAuth2GrantType.REFRESH_TOKEN) {
            tokenResponse = authorizationService.getTokenResponseForRefreshTokenGrant(authorizationDetails, oAuth2TokenRequest.getRefreshToken());
        }

        // We should never get here as validations would've ensured response type value
        if (tokenResponse == null) {
            throw new OAuth2Exception(ErrorType.OAuth2.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok(tokenResponse).build();
    }

    private Set<String> getRequestedScopes(String scopeStr) {
        Set<String> requestedScopes = null;
        if (!StringUtils.isEmpty(scopeStr)) {
            requestedScopes = Stream.of(scopeStr.split(",")).collect(Collectors.toSet());
        }

        return requestedScopes;
    }

    private Response getOAuth2AuthorizationCodeResponse(OAuth2AuthorizationRequest oAuth2AuthorizationRequest,
                                                        AuthenticatedUser authenticatedUser) throws AuthorizationException {

        Set<String> requestedScopes = getRequestedScopes(oAuth2AuthorizationRequest.getScope());
        String code = authorizationService.generateAuthorizationCode(oAuth2AuthorizationRequest.getClientId(),
                requestedScopes, oAuth2AuthorizationRequest.getRedirectUri(), authenticatedUser);

        URI requestedUri = URI.create(oAuth2AuthorizationRequest.getRedirectUri());
        URI resolvedRedirectUri = UriBuilder.fromUri(requestedUri)
                .queryParam("code", code)
                .build();

        return Response.temporaryRedirect(resolvedRedirectUri).build();
    }

    private Response getOauth2AuthorizationTokenResponse(OAuth2AuthorizationRequest oAuth2AuthorizationRequest,
                                                         AuthenticatedUser authenticatedUser) throws AuthorizationException {

        Set<String> requestedScopes = getRequestedScopes(oAuth2AuthorizationRequest.getScope());
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForImplicitGrant(oAuth2AuthorizationRequest.getClientId(),
                requestedScopes, authenticatedUser);

        URI requestedUri = URI.create(oAuth2AuthorizationRequest.getRedirectUri());
        URI resolvedRedirectUri = UriBuilder.fromUri(requestedUri)
                .fragment(oAuth2TokenResponse.getAsUriFragment())
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
