package io.keystash.common.models.common;

import io.keystash.common.models.authentication.oidc.OidcAuthenticationRequest;
import io.keystash.common.models.authentication.oidc.OidcScope;
import io.keystash.common.models.oauth.OAuth2AuthorizationRequest;

import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.BeanParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper for request object parameters
 */
public class AuthorizationRequest {

    /**
     * The OAuth2.0 specific parameters for an authorization request
     */
    @BeanParam
    @Getter @Setter private OAuth2AuthorizationRequest oAuth2AuthorizationRequest;

    /**
     * The OpenID Connect specific parameters for an authorization request
     */
    @BeanParam
    @Getter @Setter private OidcAuthenticationRequest oidcAuthenticationRequest;

    /**
     * URI Info for the request
     */
    @Context
    @Setter private UriInfo uriInfo;

    /**
     * Indicates if this request includes an OpenID Connect Authentication request
     * @return true if is an OpenID Connect request, false otherwise
     */
    public boolean isOpenIdConnectAuthRequest() {
        if (oAuth2AuthorizationRequest.getScope() != null) {
            Set<String> requestedScope = Stream.of(oAuth2AuthorizationRequest.getScope().split(",")).collect(Collectors.toSet());
            return requestedScope.contains(OidcScope.OPEN_ID_SCOPE.getValue());
        }

        return false;
    }

    /**
     * Gets the base URI of the request
     * @return a URI object representing the base URI
     */
    public URI getRequestBaseUri() {
        return uriInfo.getBaseUri();
    }

    /**
     * Gets the absolute URI of the request
     * @return a URI object representing the absolute URI
     */
    public URI getRequestAbsoluteUri() {
        return uriInfo.getAbsolutePath();
    }

    /**
     * Gets the host name of the request
     * @return a string representing a valid URI host
     */
    public String getRequestHost() {
        return getRequestBaseUri().getHost();
    }

    /**
     * Gets the raw query parameters of the request
     * @return a map of string lists representing query parameters
     */
    public Map<String, List<String>> getRequestParameters() {
        return uriInfo.getQueryParameters();
    }

}
