package io.keystash.services.jose;

import io.keystash.models.jose.SigningKey;

import java.util.Set;

/**
 * Manages all application keys
 * <p>We should use this to get and manage keys that are used for authorization purposes.</p>
 */
public interface KeyManager {

    /**
     * Gets the currently active key
     * <p>Only one active key should be allowed at any given time, otherwise there would be inconsistency in
     * token signatures</p>
     * @return the active signing key
     */
    SigningKey getActiveKey();

    /**
     * Gets all passive keys
     * <p>Passive keys are keys that were previously used to sign tokens, but have since been "retired". The
     * keys are still used to verify tokens, however they should no longer be used to sign new keys</p>
     * @return a set of passive keys
     */
    Set<SigningKey> getPassiveKeys();

    /**
     * Gets all disabled keys
     * <p>Disabled keys are no longer used at all for signatures or verification</p>
     * @return a set of disabled keys
     */
    Set<SigningKey> getDisabledKeys();

    /**
     * Gets all keys regardless of status
     * @return a set of signing keys
     */
    Set<SigningKey> getAllKeys();

}
