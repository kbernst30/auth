package io.keystash.common.models.jose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * Representation of a set of JSON WEb Keys
 * See also {@link io.keystash.common.models.jose.JsonWebKey}
 */
@ToString
@EqualsAndHashCode
public class JwkSet {

    /**
     * The set of JWk objects
     */
    @JsonProperty("keys")
    @Getter @Setter private Set<JsonWebKey> keys;

}
