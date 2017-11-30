package ca.bernstein.models.oauth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.ws.rs.FormParam;

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
     * The email belonging to the resource owner
     * <p>This will be ignored if grantType is not equal to {@code OAuth2GrantType.PASSWORD}</p>
     */
    @FormParam("email")
    @Getter @Setter private String username;

    /**
     * The password associated with the email belonging to the resource owner
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

}
