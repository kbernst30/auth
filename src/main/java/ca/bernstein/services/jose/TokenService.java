package ca.bernstein.services.jose;

import ca.bernstein.exceptions.authorization.TokenException;

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
     * Examines a given token to determine if it is still valid for use
     * @param token the token to be validates
     * @return true if the token is valid, false otherwise
     */
    boolean isTokenValid(String token);
}