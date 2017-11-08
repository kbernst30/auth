package ca.bernstein.factories.jose;

import ca.bernstein.exceptions.authorization.SigningKeyException;
import ca.bernstein.models.jose.HmacKey;
import ca.bernstein.models.jose.RsaKey;
import ca.bernstein.models.jose.SigningKey;
import ca.bernstein.services.jose.KeyManager;
import com.auth0.jwt.algorithms.Algorithm;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;

/**
 * Creates a new Algorithm object to be used in signing JWT
 */
public class JwsAlgorithmFactory {

    private final KeyManager keyManager;

    @Inject
    public JwsAlgorithmFactory(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public Algorithm createAlgorithmForSignature() throws SigningKeyException {

        Algorithm algorithm = createAlgorithm(this.keyManager.getActiveKey());
        if (algorithm == null) {
            throw new SigningKeyException("No valid key was found to create an algorithm for.");
        }

        return algorithm;
    }

    private Algorithm createAlgorithm(SigningKey signingKey) throws SigningKeyException {
        if (signingKey == null) {
            return null;
        }

        try {
            switch (signingKey.getAlgorithm()) {
                case HMAC:
                    HmacKey hmacKey = (HmacKey) signingKey;
                    return Algorithm.HMAC256(hmacKey.getSecret());
                case RSA:
                    RsaKey rsaKey = (RsaKey) signingKey;
                    return Algorithm.RSA256(rsaKey.getPublicKey(), rsaKey.getPrivateKey());
                default:
                    return null;
            }
        } catch (UnsupportedEncodingException e) {
            throw new SigningKeyException(String.format("Failed to create algorithm using given signing key [%s]", signingKey), e);
        }
    }
}
