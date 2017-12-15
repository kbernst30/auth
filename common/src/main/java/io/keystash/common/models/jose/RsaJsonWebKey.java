package io.keystash.common.models.jose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Base64;

/**
 * A JSON Web Key that uses RSA algorithm and a public RSA key
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class RsaJsonWebKey extends JsonWebKey {

    /**
     * The modulus value for the RSA public key, represented as a Base64urlUInt-encoded value
     */
    @JsonProperty("n")
    @Getter private final String modulus;

    /**
     * The exponent value for the RSA public key, represented as a Base64urlUInt-encoded value
     */
    @JsonProperty("e")
    @Getter private final String exponent;

    public RsaJsonWebKey(BigInteger modulus, BigInteger exponent) {
        this.setAlgorithm("RS256");
        this.setKeyType(JwaKeyType.RSA);

        this.modulus = new String(Base64.getUrlEncoder().encode(modulus.toByteArray())).replace("=", "");
        this.exponent = new String(Base64.getEncoder().encode(exponent.toByteArray()));
    }
}
