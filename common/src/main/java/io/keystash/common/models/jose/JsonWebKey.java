package io.keystash.common.models.jose;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * Representation of a JSON Web Key used for signatures and encryption as defined by RFC7517
 * <p>
 *     @see <a href="https://tools.ietf.org/html/rfc7517#section-4">https://tools.ietf.org/html/rfc7517#section-4</a>
 * </p>
 * <p>
 *     JSON Web Keys (JWKs) is an object that represents a cryptographic key, in JSON format. JWKs generally have
 *     fields that are key specific. This class will defined the common parameters that all JWKs should have.
 * </p>
 */
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class JsonWebKey {

    /**
     * The key type parameter, which identifies the cryptographic algorithm family used with the key. i.e RSA
     */
    @JsonProperty("kty")
    @Getter @Setter private JwaKeyType keyType;

    /**
     * The intended use of the public key, in other words for signature or for encryption
     * <p>Values should be either sig (for signature) or enc (for encryption)</p>
     */
    @JsonProperty("use")
    @Getter @Setter private String use;

    /**
     * The operations for which the key is intended to be used
     */
    @JsonProperty("key_ops")
    @Getter @Setter private Set<JwkOperation> keyOperations;

    /**
     * The algorithm to be used with this key. i.e. RS256 or HS256
     */
    @JsonProperty("alg")
    @Getter @Setter private String algorithm;

    /**
     * The key ID used for matching to a specific key
     */
    @JsonProperty("kid")
    @Getter @Setter private String keyId;

    /**
     * The URI the refers to to a resource for an X.509 public key certificate or certificate chain
     */
    @JsonProperty("x5u")
    @Getter @Setter private String x509Uri;

    /**
     * The chain of one or more PKIX certificates, represented as an array of certificate value strings
     */
    @JsonProperty("x5c")
    @Getter @Setter private Set<String> x509CertificateChain;

    /**
     * A base64-url encoded SHA-1 thumbprint of the DER encoding of an X.509 certificate
     */
    @JsonProperty("x5t")
    @Getter @Setter private String x509CertificateSha1Thumbprint;

    /**
     * A base64-url encoded SHA-256 thumbprint of the DER encoding of an X.509 certificate
     */
    @JsonProperty("x5t#S256")
    @Getter @Setter private String x509CertificateSha256Thumbprint;

}
