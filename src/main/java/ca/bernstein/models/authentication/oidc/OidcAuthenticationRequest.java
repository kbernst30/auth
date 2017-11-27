package ca.bernstein.models.authentication.oidc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 * A request object for an OpenID Connect authentication request
 * <p>
 *     OpenID Connect authentication requests ARE OAuth2.0 authorization requests that specify additional parameters
 *     @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.3.1.2.1">OpenID Connect Core Specification</a>
 * </p>
 */
@ToString
public class OidcAuthenticationRequest {

    /**
     * The mechanism to be used for returning parameters from the authorization request
     */
    @QueryParam("response_mode")
    @Getter @Setter private String responseMode;

    /**
     * A value used to associate a client session with an ID Token
     */
    @QueryParam("nonce")
    @Getter @Setter private String nonce;

    /**
     * Specifies how authentication and consent user interface pages should be displayed to End-Users
     * <p>
     *     If not specified, the default should be "page"
     * </p>
     */
    @QueryParam("display")
    @DefaultValue("page")
    @Getter @Setter private OidcUserInterfaceDisplay display;

    /**
     * Specifies whether or not End-user should be prompted for re-authentication and consent
     */
    @QueryParam("prompt")
    @Getter @Setter private String prompt;

    /**
     * The maximum elapsed time allowed in seconds before the end-user must re-authenticate
     */
    @QueryParam("max_age")
    @Getter @Setter private Long maxAge;

    /**
     * The preferred languages/scripts of the End-user for user interface pages, in order of preference
     * <p>These are specified by a space separated list</p>
     * <p>If any locale is specified but not supported, it will be ignored</p>
     */
    @QueryParam("ui_locales")
    @Getter @Setter private String uiLocales;

    /**
     * The ID token that was previously issued, passed as a hint about the End-User's current or past session with the client
     */
    @QueryParam("id_token_hint")
    @Getter @Setter private String idTokenHint;

    /**
     * Hint about the login identified (i.e. email address) the End-User uses to log in
     */
    @QueryParam("login_hint")
    @Getter @Setter private String loginHint;

    /**
     * Request authentication context class ref values
     * <p>These are specified by a space separated list</p>
     * <p>The context class satisfied by the authentication will be returns as an acr claim in the token</p>
     */
    @QueryParam("acr_values")
    @Getter @Setter private String acrValues;
}
