package io.keystash.services.jose;

/**
 * Provides keys that use a static secret
 */
public interface SecretKeyProvider extends KeyProvider {

    /**
     * Gets the secret to be used in the key signature
     * @return the secret string
     */
    String getSecret();

}
