package ca.bernstein.models.jose;

import lombok.*;

/**
 * Represents a key with a secret, generated using a HMAC algorithm
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class HmacKey implements SecretKey {

    @Getter @Setter private String secret;

    @Override
    public JwsAlgorithmType getAlgorithm() {
        return JwsAlgorithmType.HMAC;
    }
}
