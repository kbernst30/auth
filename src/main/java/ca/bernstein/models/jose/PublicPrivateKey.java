package ca.bernstein.models.jose;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * A key that uses both a public and private key in its signing algorithm
 */
public interface PublicPrivateKey extends SigningKey {

    /**
     * Gets the public key used in the algorithm
     * @return a public key
     */
    PublicKey getPublicKey();

    /**
     * Gets the private key used in the algorithm
     * @return a private key
     */
    PrivateKey getPrivateKey();

}
