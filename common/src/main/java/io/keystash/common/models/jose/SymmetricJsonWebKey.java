package io.keystash.common.models.jose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Base64;

/**
 * A JSON web key that uses a symmetric key (such as HMAC)
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class SymmetricJsonWebKey extends JsonWebKey {

    /**
     * The key value used in a symmetric algorithm
     */
    @JsonProperty("k")
    @Getter private final String keyValue;

    public SymmetricJsonWebKey(String keyValue, String algorithm) {
        this.setAlgorithm(algorithm);
        this.setKeyType(JwaKeyType.OCT);

        this.keyValue = new String(Base64.getUrlEncoder().encode(keyValue.getBytes())).replace("=", "");
    }
}
