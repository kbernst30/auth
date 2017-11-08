package ca.bernstein.models.jose;

import lombok.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Represents a key with public/private keys, generated using an RSA algorithm
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class RsaKey implements PublicPrivateKey {

    @Getter @Setter private final RSAPublicKey publicKey;
    @Getter @Setter private final RSAPrivateKey privateKey;

    @Override
    public JwsAlgorithmType getAlgorithm() {
        return JwsAlgorithmType.RSA;
    }
}
