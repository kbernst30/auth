package io.keystash.models.common;

import io.keystash.models.authentication.oidc.OidcAuthenticationRequest;
import io.keystash.models.oauth.OAuth2AuthorizationRequest;
import io.keystash.util.AuthenticationUtils;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.BeanParam;
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
     * Indicates if this request includes an OpenID Connect Authentication request
     * @return true if is an OpenID Connect request, false otherwise
     */
    public boolean isOpenIdConnectAuthRequest() {
        if (oAuth2AuthorizationRequest.getScope() != null) {
            Set<String> requestedScope = Stream.of(oAuth2AuthorizationRequest.getScope().split(",")).collect(Collectors.toSet());
            return requestedScope.contains(AuthenticationUtils.OPEN_ID_SCOPE);
        }

        return false;
    }

}
