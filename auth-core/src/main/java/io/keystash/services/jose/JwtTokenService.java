package io.keystash.services.jose;

import io.keystash.common.exceptions.SigningKeyException;
import io.keystash.common.models.jose.HmacKey;
import io.keystash.common.models.jose.JwsAlgorithmType;
import io.keystash.common.models.jose.SigningKey;
import io.keystash.exceptions.authorization.TokenException;
import io.keystash.factories.jose.JwsAlgorithmFactory;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.models.web.HostInfo;
import io.keystash.util.AuthenticationUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Provider;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
public class JwtTokenService implements TokenService {

    private final JwsAlgorithmFactory jwsAlgorithmFactory;
    private final KeyManager keyManager;
    private final Provider<HostInfo> hostInfoProvider;

    @Inject
    public JwtTokenService(JwsAlgorithmFactory jwsAlgorithmFactory, KeyManager keyManager, Provider<HostInfo> hostInfoProvider) {
        this.jwsAlgorithmFactory = jwsAlgorithmFactory;
        this.keyManager = keyManager;
        this.hostInfoProvider = hostInfoProvider;
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
            return jwtCreator.sign(jwsAlgorithmFactory.createAlgorithmForSignature(this.keyManager.getActiveKey()));
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
            return jwtCreator.sign(jwsAlgorithmFactory.createAlgorithmForSignature(this.keyManager.getActiveKey()));
        } catch (SigningKeyException e) {
            throw new TokenException("Failed to create a JWT due to a problem with the signing key", e);
        }
    }

    @Override
    public String createIdToken(String clientId, AuthenticatedUser authenticatedUser, String accessToken, String code,
                                String nonce, int expiryTimeSeconds) throws TokenException {

        HostInfo hostInfo = hostInfoProvider.get();

        JWTCreator.Builder jwtCreator = JWT.create();
        Date now = new Date();

        jwtCreator.withIssuer(hostInfo.getBaseUrl());
        jwtCreator.withSubject(AuthenticationUtils.getSubjectIdentifierForUser(authenticatedUser));
        jwtCreator.withAudience(clientId);
        jwtCreator.withExpiresAt(new DateTime(now).plusSeconds(expiryTimeSeconds).toDate());
        jwtCreator.withIssuedAt(now);

        if (!StringUtils.isEmpty(nonce)) {
            jwtCreator.withClaim("nonce", nonce);
        }

        jwtCreator.withClaim("email", authenticatedUser.getEmail());
        jwtCreator.withClaim("user_id", authenticatedUser.getUserId());

        try {
            SigningKey activeKey = this.keyManager.getActiveKey();
            Algorithm idTokenAlgorithm = jwsAlgorithmFactory.createAlgorithmForSignature(activeKey);

            if (!StringUtils.isEmpty(accessToken)) {
                jwtCreator.withClaim("at_hash", getHashFromSourceForIdToken(accessToken, activeKey));
            }

            if (!StringUtils.isEmpty(code)) {
                jwtCreator.withClaim("c_hash", getHashFromSourceForIdToken(code, activeKey));
            }

            return jwtCreator.sign(idTokenAlgorithm);
        } catch (SigningKeyException e) {
            throw new TokenException("Failed to create a JWT due to a problem with the signing key", e);
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Set<Algorithm> algorithmsForVerification = jwsAlgorithmFactory.createAlgorithmsForVerification(this.keyManager.getActiveKey(),
                    this.keyManager.getPassiveKeys());

            for (Algorithm algorithm : algorithmsForVerification) {
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

    private String getHashFromSourceForIdToken(String source, SigningKey signingKey) {

        // Hash the token with the algorithm provided (same as used to sign ID token)
        byte[] hashedTokenBytes;
        if (signingKey.getAlgorithm() == JwsAlgorithmType.RSA) {
            hashedTokenBytes = getRsaSourceHash(source);
        } else if (signingKey.getAlgorithm() == JwsAlgorithmType.HMAC){
            hashedTokenBytes = getHmacSourceHash(source, ((HmacKey) signingKey).getSecret());
        } else {
            throw new RuntimeException("An invalid algorithm was found when generating hashes for id token.");
        }

        // Take the left most half of the hash. For example, if there are 256 bits (from SHA-256 maybe) then take 128 bits
        int bytesToTake = hashedTokenBytes.length / 2;
        byte[] significantBytes = new byte[bytesToTake];
        System.arraycopy(hashedTokenBytes, 0, significantBytes, 0, bytesToTake);

        return new String(Base64.getEncoder().encode(significantBytes));
    }

    private byte[] getRsaSourceHash(String source) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to hash source using RSA256", e);
        }

        return sha256.digest(source.getBytes());
    }

    private byte[] getHmacSourceHash(String source, String secret) {
        Mac hmacShas256;
        try {
            hmacShas256 = Mac.getInstance("HmacSHA256");
            hmacShas256.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Unable to hash source using HmacSHA256", e);
        }

        return hmacShas256.doFinal(source.getBytes());
    }
}
