package io.keystash.factories.jose;

import io.keystash.common.models.jose.*;

/**
 * Creates JSON Web Key instances from signing keys of different algorithms
 */
public class JwkFactory {

    public JsonWebKey createJsonWebKey(SigningKey signingKey) {
        JsonWebKey jsonWebKey = null;
        if (signingKey.getAlgorithm() == JwsAlgorithmType.RSA) {
            RsaKey rsaKey = (RsaKey) signingKey;
            jsonWebKey = new RsaJsonWebKey(rsaKey.getPublicKey().getModulus(), rsaKey.getPublicKey().getPublicExponent());
        } else if (signingKey.getAlgorithm() == JwsAlgorithmType.HMAC) {
            HmacKey hmacKey = (HmacKey) signingKey;
            jsonWebKey = new SymmetricJsonWebKey(hmacKey.getSecret(), "HS256");
        }

        if (jsonWebKey != null) {
            jsonWebKey.setKeyId(signingKey.getKid());
            jsonWebKey.setUse("sig");
        }

        return jsonWebKey;
    }

}
