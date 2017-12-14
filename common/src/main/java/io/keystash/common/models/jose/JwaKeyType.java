package io.keystash.common.models.jose;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents valid types of Json Web Algorithm Keys
 * <p>
 *     @see <a href="https://tools.ietf.org/html/rfc7518">https://tools.ietf.org/html/rfc7518</a>
 * </p>
 */
public enum JwaKeyType {

    EC("EC"),
    RSA("RSA"),
    OCT("oct");

    private final String ktyValue;

    JwaKeyType(String ktyValue) {
        this.ktyValue = ktyValue;
    }

    @JsonValue
    public String getKtyValue() {
        return ktyValue;
    }
}
