package ca.bernstein.models.jose;

import lombok.Getter;

public enum JwsAlgorithmType {
    RSA(null),
    HMAC("HmacSHA256"),
    AES("AES"),
    ECDSA(null);

    @Getter private final String javaAlgorithm;

    JwsAlgorithmType(String javaAlgorithm) {
        this.javaAlgorithm = javaAlgorithm;
    }


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
