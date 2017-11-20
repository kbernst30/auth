package ca.bernstein.services.jose;

import ca.bernstein.exceptions.authorization.SigningKeyException;
import ca.bernstein.exceptions.authorization.TokenException;
import ca.bernstein.factories.jose.JwsAlgorithmFactory;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
}
