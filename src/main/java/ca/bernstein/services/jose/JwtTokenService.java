package ca.bernstein.services.jose;

import ca.bernstein.exceptions.authorization.SigningKeyException;
import ca.bernstein.exceptions.authorization.TokenException;
import ca.bernstein.factories.jose.JwsAlgorithmFactory;
import ca.bernstein.models.authentication.AuthenticatedUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class JwtTokenService implements TokenService {

    private final JwsAlgorithmFactory jwsAlgorithmFactory;

    @Inject
    public JwtTokenService(JwsAlgorithmFactory jwsAlgorithmFactory) {
        this.jwsAlgorithmFactory = jwsAlgorithmFactory;
    }

    @Override
    public String createAccessToken(Map<String, String> claims, int expiryTimeInSeconds) throws TokenException {
        JWTCreator.Builder jwtCreator = JWT.create();
        Date now = new Date();

        // Create some defaults
        jwtCreator.withJWTId(UUID.randomUUID().toString());
        jwtCreator.withIssuedAt(now);
        jwtCreator.withExpiresAt(new DateTime(now).plusSeconds(expiryTimeInSeconds).toDate());

        // Add all claims to the builder
        // If caller specified reserved claims, use those to override defaults
        claims.forEach(jwtCreator::withClaim);

        try {
            return jwtCreator.sign(jwsAlgorithmFactory.createAlgorithmForSignature());
        } catch (SigningKeyException e) {
            throw new TokenException("Failed to create a JWT due to a problem with the signing key", e);
        }
    }

    @Override
    public String createRefreshToken(String accessToken) throws TokenException {
        DecodedJWT decodedAccessToken = JWT.decode(accessToken);
        JWTCreator.Builder jwtCreator = JWT.create();

        decodedAccessToken.getClaims().forEach((string, claim) -> jwtCreator.withClaim(string, claim.asString()));

        Date now = new Date();

        // Create some defaults
        jwtCreator.withJWTId(UUID.randomUUID().toString());
        jwtCreator.withIssuedAt(now);
        jwtCreator.withExpiresAt(new DateTime(decodedAccessToken.getExpiresAt()).plusDays(7).toDate());
        jwtCreator.withClaim("ati", decodedAccessToken.getId());

        try {
            return jwtCreator.sign(jwsAlgorithmFactory.createAlgorithmForSignature());
        } catch (SigningKeyException e) {
            throw new TokenException("Failed to create a JWT due to a problem with the signing key", e);
        }
    }

    @Override
    public String createIdToken(String clientId, AuthenticatedUser authenticatedUser, String accessToken,
                                String nonce, int expiryTimeSeconds) throws TokenException {

        JWTCreator.Builder jwtCreator = JWT.create();
        Date now = new Date();

        jwtCreator.withIssuer("http://localhost:8080/"); // TODO get actual issuer - config or detect?
        jwtCreator.withSubject(getSubjectIdentifierForUser(authenticatedUser));
        jwtCreator.withAudience(clientId);
        jwtCreator.withExpiresAt(new DateTime(now).plusSeconds(expiryTimeSeconds).toDate());
        jwtCreator.withIssuedAt(now);

        if (!StringUtils.isEmpty(nonce)) {
            jwtCreator.withClaim("nonce", nonce);
        }

        jwtCreator.withClaim("username", authenticatedUser.getUsername());
        jwtCreator.withClaim("account_id", authenticatedUser.getUserId());

        try {
            Algorithm idTokenAlgorithm = jwsAlgorithmFactory.createAlgorithmForSignature();

            if (!StringUtils.isEmpty(accessToken)) {
                jwtCreator.withClaim("at_hash", getAccessTokenHashForIdToken(accessToken, idTokenAlgorithm));
            }

            return jwtCreator.sign(idTokenAlgorithm);
        } catch (SigningKeyException e) {
            throw new TokenException("Failed to create a JWT due to a problem with the signing key", e);
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            for (Algorithm algorithm : jwsAlgorithmFactory.createAlgorithmsForVerification()) {
                JWTVerifier verifier = JWT.require(algorithm).build();

                // This is not ideal... But due to Auth0's JWT implementation, we have to run the try/catch here as
                // the recommended way to verify token is to check if the verifier throws an exception :s
                try {
                    verifier.verify(token);
                    return true;
                } catch (JWTVerificationException e) {
                    log.debug("Token was not valid... skipping...");
                }
            }
        } catch (SigningKeyException e) {
            log.warn("Unable to properly create verification algorithm using found passive/active keys. " +
                    "Marking token as invalid as a result.");
        }

        return false;
    }

    @Override
    public String getTokenClaim(String token, String claim) {
        DecodedJWT decodedAccessToken = JWT.decode(token);
        return decodedAccessToken.getClaim(claim).asString();
    }

    private String getSubjectIdentifierForUser(AuthenticatedUser authenticatedUser) throws TokenException {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new TokenException("Unable to generate subject identifier for user", e);
        }

        String salt = authenticatedUser.getUsername() + ":" + authenticatedUser.getUserId(); // TODO more secure/random salt
        byte[] hash = sha256.digest(salt.getBytes());
        return UUID.nameUUIDFromBytes(hash).toString();
    }

    private String getAccessTokenHashForIdToken(String accessToken, Algorithm algorithm) throws TokenException {
        // Hash the token with the algorithm provided (same as used to sign ID token)
        byte[] hashedTokenBytes = algorithm.sign(accessToken.getBytes());

        // Take the left most 128 bits (first 16 bytes)
        if (hashedTokenBytes.length < 16) {
            throw new TokenException(String.format("Unable to get access token hash for ID token using algorithim [%s]",
                    algorithm.getName()));
        }

        byte[] significantBytes = new byte[16];
        System.arraycopy(hashedTokenBytes, 0, significantBytes, 0, 16);

        return new String(Base64.getEncoder().encode(significantBytes));
    }
}
