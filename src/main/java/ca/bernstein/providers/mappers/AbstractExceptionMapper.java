package ca.bernstein.providers.mappers;

import ca.bernstein.exceptions.AbstractWebApplicationException;
import ca.bernstein.models.common.AuthorizationRequest;
import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.oauth.OAuth2AuthorizationRequest;
import ca.bernstein.models.oauth.OAuth2ResponseType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * An abstract exception mapper
 */
@Slf4j
public abstract class AbstractExceptionMapper<T extends AbstractWebApplicationException> implements ExceptionMapper<T> {

    @Context
    private Provider<AuthorizationRequest> authorizationRequestProvider;

    @Override
    public Response toResponse(T e) {
        if (e.getResponse().getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            log.error(e.getMessage());
        } else {
            log.warn(e.getMessage());
        }

        if (StringUtils.isEmpty(e.getRedirectUri()) || authorizationRequestProvider.get() == null) {
            return Response.fromResponse(e.getResponse())
                    .entity(e.getError())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }

        return Response.seeOther(buildRedirectUri(e.getRedirectUri(), e.getError()))
                .build();
    }

    /**
     * Builds an appropriate redirect URI using the provided uri string and error response
     * @param uriStr the value to construct the URI from
     * @param errorResponse the error object to use as part of the URI
     * @return a new URI
     */
    private URI buildRedirectUri(String uriStr, ErrorResponse errorResponse) {
        URI redirectUri;

        try {
            redirectUri = new URI(uriStr);
        } catch (URISyntaxException e) {
            log.warn("URI string [{}] is invalid", uriStr, e);
            return null;
        }

        AuthorizationRequest authorizationRequest = authorizationRequestProvider.get();
        UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);

        if (authorizationRequest != null && authorizationRequest.getOAuth2AuthorizationRequest() != null) {
            OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
            String state = oAuth2AuthorizationRequest.getState();

            if (oAuth2AuthorizationRequest.getResponseTypes().size() == 1
                    && oAuth2AuthorizationRequest.getResponseTypes().contains(OAuth2ResponseType.CODE)) {

                uriBuilder.replaceQuery(errorResponse.getAsUrlEncodedFormParams());
                if (!StringUtils.isEmpty(state)) {
                    uriBuilder.queryParam("state", state);
                }

            } else {
                String fragment = errorResponse.getAsUrlEncodedFormParams();
                if (!StringUtils.isEmpty(state)) {
                    fragment += "&state=" + state;
                }

                uriBuilder.fragment(fragment);
            }
        }

        return uriBuilder.build();
    }


}
