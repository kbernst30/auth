package io.keystash.common.util;

import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.error.ErrorType;
import io.keystash.common.services.jose.TokenService;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationUtils {

    /**
     * Returns a set of strings representing the scopes found in the string parameter
     * @param scope a comma separated string of scopes
     * @return a set of scope strings
     */
    public static Set<String> getScopes(String scope) {
        Set<String> requestedScopes = null;
        if (!StringUtils.isEmpty(scope)) {
            requestedScopes = Stream.of(scope.split(",")).collect(Collectors.toSet());
        }

        return requestedScopes;
    }

    public static String getSubjectIdentifierForUser(AuthenticatedUser authenticatedUser) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Hardcoding the algorithm should mean we never get here
            throw new RuntimeException("Unable to generate subject identifier for user", e);
        }

        String salt = authenticatedUser.getEmail() + ":" + authenticatedUser.getUserId(); // TODO more secure/random salt
        byte[] hash = sha256.digest(salt.getBytes());
        return UUID.nameUUIDFromBytes(hash).toString();
    }

    public static String getAccessTokenFromHeader(String authorizationHeader, TokenService tokenService) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_ACCESS_TOKEN, Response.Status.UNAUTHORIZED);
        }

        String[] headerParts = authorizationHeader.split(" ");
        if (headerParts.length != 2 || !headerParts[0].toLowerCase().equals("bearer")) {
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_ACCESS_TOKEN, Response.Status.UNAUTHORIZED);
        }

        String token = headerParts[1];
        if (!tokenService.isTokenValid(token)) {
            throw new OAuth2Exception(ErrorType.OAuth2.EXPIRED_ACCESS_TOKEN, Response.Status.UNAUTHORIZED);
        }

        return token;
    }
}
