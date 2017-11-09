package ca.bernstein.resources.authorization;

import ca.bernstein.exceptions.OAuth2WebException;
import ca.bernstein.exceptions.authorization.AuthorizationException;
import ca.bernstein.exceptions.authorization.InvalidScopeException;
import ca.bernstein.exceptions.authorization.UnauthorizedClientException;
import ca.bernstein.exceptions.authorization.UnknownClientException;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.models.oauth.OAuth2AuthorizationRequest;
import ca.bernstein.models.oauth.OAuth2GrantType;
import ca.bernstein.models.oauth.OAuth2ResponseType;
import ca.bernstein.models.oauth.OAuth2TokenResponse;
import ca.bernstein.services.authorization.OAuth2AuthorizationService;
import ca.bernstein.util.Validations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOauth2Authorization(@BeanParam OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {

        Validations.validateOAuth2AuthorizationRequest(oAuth2AuthorizationRequest);

        try {
            if (oAuth2AuthorizationRequest.getResponseType() == OAuth2ResponseType.CODE) {
                return getOAuth2AuthorizationCodeResponse(oAuth2AuthorizationRequest);
            } else if (oAuth2AuthorizationRequest.getResponseType() == OAuth2ResponseType.TOKEN) {
                return getOauth2AuthorizationTokenResponse(oAuth2AuthorizationRequest);
            }

            // We should never get here as validations would've ensured response type value
            throw new OAuth2WebException(ErrorType.OAuth2.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);

        } catch (UnknownClientException e) {
            log.warn("No client was found for clientId [{}]", oAuth2AuthorizationRequest.getClientId(), e);
            throw new OAuth2WebException(ErrorType.OAuth2.UNKNOWN_CLIENT_ID, Response.Status.BAD_REQUEST,
                    oAuth2AuthorizationRequest.getClientId());

        } catch (UnauthorizedClientException e) {
            log.warn("Client [{}] requested an authorization code but is not allowed to authorize in this way",
                    oAuth2AuthorizationRequest.getClientId(), e);
            throw new OAuth2WebException(ErrorType.OAuth2.GRANT_TYPE_NOT_ALLOWED, Response.Status.BAD_REQUEST,
                    oAuth2AuthorizationRequest.getClientId(), OAuth2GrantType.AUTHORIZATION_CODE.name().toLowerCase());

        } catch (InvalidScopeException e) {
            log.warn("Client [{}] requested an invalid scope [{}]", oAuth2AuthorizationRequest.getClientId(), e.getScope(), e);
            throw new OAuth2WebException(ErrorType.OAuth2.INVALID_SCOPE, Response.Status.BAD_REQUEST,
                    oAuth2AuthorizationRequest.getClientId(), e.getScope());

        } catch (AuthorizationException e) {
            log.error("An unexpected error occurred processing OAuth2.0 authorization", e);
            throw new OAuth2WebException(ErrorType.OAuth2.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Set<String> getRequestedScopes(String scopeStr) {
        Set<String> requestedScopes = null;
        if (!StringUtils.isEmpty(scopeStr)) {
            requestedScopes = Stream.of(scopeStr.split(",")).collect(Collectors.toSet());
        }

        return requestedScopes;
    }

    private Response getOAuth2AuthorizationCodeResponse(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) throws AuthorizationException {
        Set<String> requestedScopes = getRequestedScopes(oAuth2AuthorizationRequest.getScope());
        String code = authorizationService.generateAuthorizationCode(oAuth2AuthorizationRequest.getClientId(), requestedScopes);

        URI requestedUri = URI.create(oAuth2AuthorizationRequest.getRedirectUri());
        URI resolvedRedirectUri = UriBuilder.fromUri(requestedUri)
                .queryParam("code", code)
                .build();

        return Response.temporaryRedirect(resolvedRedirectUri).build();
    }

    private Response getOauth2AuthorizationTokenResponse(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) throws AuthorizationException {
        Set<String> requestedScopes = getRequestedScopes(oAuth2AuthorizationRequest.getScope());

        // todo this should use session info
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForImplicitGrant(1, "test@test.com",
                oAuth2AuthorizationRequest.getClientId(), requestedScopes);

        URI requestedUri = URI.create(oAuth2AuthorizationRequest.getRedirectUri());
        URI resolvedRedirectUri = UriBuilder.fromUri(requestedUri)
                .fragment(oAuth2TokenResponse.getAsUriFragment())
                .build();

        return Response.temporaryRedirect(resolvedRedirectUri).build();
    }
}
