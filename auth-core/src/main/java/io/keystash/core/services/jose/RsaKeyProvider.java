package io.keystash.core.services.jose;

import io.keystash.common.models.jose.JwsAlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@AllArgsConstructor
public class RsaKeyProvider implements PublicPrivateKeyProvider {

    @Getter private final String keyId;
    @Getter private final RSAPublicKey publicKey;
    @Getter private final RSAPrivateKey privateKey;
    @Getter private final boolean isActive;
    @Getter private final boolean isPassive;

    @Override
    public JwsAlgorithmType getAlgorithmType() {
        return JwsAlgorithmType.RSA;
    }

    @Override
    public String getId() {
        return keyId;
    }
}
