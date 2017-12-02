package io.keystash.services.jose;

import io.keystash.exceptions.authorization.TokenException;
import io.keystash.models.authentication.AuthenticatedUser;

import java.util.Map;

/**
 * Manages all token related logic, including generation and validation
 */
public interface TokenService {

    /**
     * Generates an access token using the provided public claim items
     * @param claims the claims to be included in the token
     * @param expiryTimeSeconds the amount of time in seconds that the token will be valid for
     * @return a new access token string
     * @throws TokenException
     */
    String createAccessToken(Map<String, String> claims, int expiryTimeSeconds) throws TokenException;

    /**
     * Generates a refresh token associated with a provided access token
     * @param accessToken the access token that the refresh token will be associated with
     * @return a new refresh token string
     * @throws TokenException
     */
    String createRefreshToken(String accessToken) throws TokenException;

    /**
     * Generates an ID token as described by OpenID Connect Core specification
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.3.1.2.1">OpenID Connect Core Specification</a>
     * @param clientId the ID of the client requesting the token
     * @param authenticatedUser the authentication details to create the token for
     * @param accessToken the access token that is being issued with this ID token
     * @param code the authorization code that is being issued with this ID token from the authorization endpoint
     * @param nonce the passed request nonce value, used to associate a session with the token
     * @param expiryTimeSeconds the amount of time in seconds that the token will be valid for
     * @return a new ID token string
     * @throws TokenException
     */
    String createIdToken(String clientId, AuthenticatedUser authenticatedUser, String accessToken, String code,
                         String nonce, int expiryTimeSeconds) throws TokenException;

    /**
     * Examines a given token to determine if it is still valid for use
     * @param token the token to be validates
     * @return true if the token is valid, false otherwise
     */
    boolean isTokenValid(String token);

    /**
     * Gets the value associated with the given token claim, or null if not defined
     * @param token the token to check the claim for
     * @param claim the claim to look up
     * @return a value representing the token claim, or null if claim does not exist
     */
    String getTokenClaim(String token, String claim);
}