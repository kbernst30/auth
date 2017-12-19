package io.keystash.core.services.jose;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Provides keys that use a public and private key
 */
public interface PublicPrivateKeyProvider extends KeyProvider {

    /**
     * Gets the public key used
     * @return a Java security Public Key
     */
    PublicKey getPublicKey();

    /**
     * Gets the private key used
     * @return a Java security Private Key
     */
    PrivateKey getPrivateKey();

}
