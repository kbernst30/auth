package ca.bernstein.services.jose;

import ca.bernstein.exceptions.authorization.SigningKeyException;
import ca.bernstein.exceptions.authorization.TokenException;
import ca.bernstein.factories.jose.JwsAlgorithmFactory;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
        return null;
    }

    @Override
    public boolean isTokenValid(String token) {
        return false;
    }
}
