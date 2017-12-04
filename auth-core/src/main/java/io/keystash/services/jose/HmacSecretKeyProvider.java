package io.keystash.services.jose;

import io.keystash.models.jose.JwsAlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class HmacSecretKeyProvider implements SecretKeyProvider {

    @Getter private final String secret;
    @Getter private final boolean active;
    @Getter private final boolean passive;

    @Override
    public JwsAlgorithmType getAlgorithmType() {
        return JwsAlgorithmType.HMAC;
    }
}
