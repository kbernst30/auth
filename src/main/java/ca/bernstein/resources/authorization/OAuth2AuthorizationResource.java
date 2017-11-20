package ca.bernstein.resources.authorization;

import ca.bernstein.annotation.AuthenticationRequired;
import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.exceptions.authorization.AuthorizationException;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.models.oauth.*;
import ca.bernstein.services.authorization.OAuth2AuthorizationService;
import ca.bernstein.util.Validations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A resource for OAuth2.0 authorization requests
 */
@Slf4j
@Path("/oauth")
public class OAuth2AuthorizationResource {

    private final OAuth2AuthorizationService authorizationService;

    @Inject
    public OAuth2AuthorizationResource(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Processes an OAuth2.0 authorize request to the /authorize endpoint
     * <p>A valid session is necessary to access this resource.</p>
     *
     * @param oAuth2AuthorizationRequest A valid OAuth2AuthorizationRequest object
     * @return A valid Response redirect to a valid specified redirect URI
     */
    @GET
    @Path("/authorize")
    @AuthenticationRequired
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOAuth2Authorization(@BeanParam OAuth2AuthorizationRequest oAuth2AuthorizationRequest,
                                           @Context AuthenticatedUser authenticatedUser) {

        Validations.validateOAuth2AuthorizationRequest(oAuth2AuthorizationRequest);

        if (oAuth2AuthorizationRequest.getResponseType() == OAuth2ResponseType.CODE) {
            return getOAuth2AuthorizationCodeResponse(oAuth2AuthorizationRequest, authenticatedUser);
        } else if (oAuth2AuthorizationRequest.getResponseType() == OAuth2ResponseType.TOKEN) {
            return getOauth2AuthorizationTokenResponse(oAuth2AuthorizationRequest, authenticatedUser);
        }

        // We should never get here as validations would've ensured response type value
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
}
