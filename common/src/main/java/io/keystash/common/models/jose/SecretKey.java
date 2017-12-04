package io.keystash.common.models.jose;

/**
 * A key that uses a secret in its algorithm
 */
public interface SecretKey extends SigningKey {

    /**
     * Fetches the secret used in the signing algorithm
     * @return a secret
     */
    String getSecret();

}
