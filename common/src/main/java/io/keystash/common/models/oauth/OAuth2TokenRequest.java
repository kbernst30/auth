package io.keystash.common.models.oauth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * A request object for an OAuth2.0 token request
 */
@ToString
public class OAuth2TokenRequest {

    /**
     * The grant type that defines this token request
     */
    @FormParam("grant_type")
    @Getter @Setter private OAuth2GrantType grantType;

    /**
     * The requested scope of the authorization
     */
    @FormParam("scope")
    @Getter @Setter private String scope;

    /**
     * A valid URI that was previously used in redirect for OAuth2.0 flow
     */
    @FormParam("redirect_uri")
    @Getter @Setter private String redirectUri;

    /**
     * The code to be used in the authorization code grant
     * <p>This will be ignored if grantType is not equal to {@code OAuth2GrantType.AUTHORIZATION_CODE}</p>
     */
    @FormParam("code")
    @Getter @Setter private String code;

    /**
     * The username belonging to the resource owner
     * <p>This will be ignored if grantType is not equal to {@code OAuth2GrantType.PASSWORD}</p>
     */
    @FormParam("username")
    @Getter @Setter private String username;

    /**
     * The password associated with the username belonging to the resource owner
     * <p>This will be ignored if grantType is not equal to {@code OAuth2GrantType.PASSWORD}</p>
     */
    @FormParam("password")
    @Getter @Setter private String password;

    /**
     * The refresh token to be used to refresh an existing authorization
     * <p>This will be ignored if grantType is not equal to {@code OAuth2GrantType.REFRESH_TOKEN}</p>
     */
    @FormParam("refresh_token")
    @Getter @Setter private String refreshToken;

    /**
     * URI Info for the request
     */
    @Context
    @Setter private UriInfo uriInfo;

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
