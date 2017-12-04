package io.keystash.services.jose;

import io.keystash.common.models.jose.JwsAlgorithmType;

/**
 * Provider for appropriate key to be used for signing tokens
 */
public interface KeyProvider {

    /**
     * Returns the algorithm to be used for JSON web signature
     * @return the algorithm type
     */
    JwsAlgorithmType getAlgorithmType();

    /**
     * Tells if the key should be active
     * @return true if the key is active, false otherwise
     */
    boolean isActive();

    /**
     * Tells if the key should be passive
     * @return true if key is passive, false otherwise
     */
    boolean isPassive();

}
