package io.keystash.core.factories.jose;

import io.keystash.common.exceptions.SigningKeyException;
import io.keystash.common.models.jose.HmacKey;
import io.keystash.common.models.jose.RsaKey;
import io.keystash.common.models.jose.SigningKey;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates a new Algorithm object to be used in signing JWT
 */
public class JwsAlgorithmFactory {

    public Algorithm createAlgorithmForSignature(SigningKey activeKey) throws SigningKeyException {

        Algorithm algorithm = createAlgorithm(activeKey);
        if (algorithm == null) {
            throw new SigningKeyException("No valid key was found to create an algorithm for.");
        }

        return algorithm;
    }

    public Set<Algorithm> createAlgorithmsForVerification(SigningKey activeKey, Set<SigningKey> passiveKeys) throws SigningKeyException {
        Set<Algorithm> algorithms = new HashSet<>();
        if (activeKey != null) {
            algorithms.add(createAlgorithm(activeKey));
        }

        for (SigningKey signingKey : passiveKeys) {
            algorithms.add(createAlgorithm(signingKey));
        }

        return algorithms;
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
