package ca.bernstein.models.oauth;

import ca.bernstein.models.authentication.AuthenticatedUser;
import lombok.*;

import javax.ws.rs.QueryParam;
import java.io.Serializable;
import java.util.Set;

/**
 * A serializable result of auth code generation
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2AuthCode implements Serializable {

    public OAuth2AuthCode(String code, String clientId, Set<String> resolvedScopes, String redirectUri, AuthenticatedUser authenticatedUser) {
        this.code = code;
        this.clientId = clientId;
        this.resolvedScopes = resolvedScopes;
        this.redirectUri = redirectUri;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * A random alphanumeric string representing an authorization code
     */
    @Getter @Setter private String code;

    /**
     * The client that the code is bound to
     */
    @Getter @Setter private String clientId;

    /**
     * A set of strings representing the scopes that the code was requested for
     * <p>These scopes will eventually carry over to the generated access token</p>
     */
    @Getter @Setter private Set<String> resolvedScopes;

    /**
     * The redirect URI that the client will redirect to with this code
     * <p>This redirect URI will be checked later. A client requesting a token using this code must supply the same
     * redirect URI</p>
     */
    @Getter @Setter private String redirectUri;

    /**
     * An authenticated user that the code is generated on behalf of
     */
    @Getter @Setter private AuthenticatedUser authenticatedUser;

    /**
     * A value used to associate a client session with an ID Token
     */
    @Getter @Setter private String nonce;

    /**
     * The ID token associated with this authorization code
     * <p>This will be null unless the client requested an authorization code using a hybrid flow with id_token</p>
     */
    @Getter @Setter private String idToken;

    /**
     * Tha access token associated with this authorization code
     * <p>This will be null unless the client requested an authorization code using a hybrid flow with token</p>
     */
    @Getter @Setter private String accessToken;

}
