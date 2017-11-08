package ca.bernstein.services.jose;

import ca.bernstein.models.jose.JwsAlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@AllArgsConstructor
public class RsaKeyProvider implements PublicPrivateKeyProvider {

    @Getter private final RSAPublicKey publicKey;
    @Getter private final RSAPrivateKey privateKey;
    @Getter private final boolean isActive;
    @Getter private final boolean isPassive;

    @Override
    public JwsAlgorithmType getAlgorithmType() {
        return JwsAlgorithmType.RSA;
    }
}
