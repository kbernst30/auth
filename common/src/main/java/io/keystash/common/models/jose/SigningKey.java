package io.keystash.common.models.jose;

/**
 * A key used to sign a JSON Web Token
 */
public interface SigningKey {

    /**
     * The algorithm used to sign the token
     * @return a type of algorithm
     */
    JwsAlgorithmType getAlgorithm();

}
