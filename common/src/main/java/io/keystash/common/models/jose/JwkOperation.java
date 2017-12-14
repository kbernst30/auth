package io.keystash.common.models.jose;

import com.fasterxml.jackson.annotation.JsonValue;

public enum JwkOperation {

    SIGN("sign"),
    VERIFY("verify"),
    ENCRYPT("encrypt"),
    DECRYPT("decrypt"),
    WRAP_KEY("wrapKey"),
    UNWRAP_KEY("unwrapKey"),
    DERIVE_KEY("deriveKey"),
    DERIVE_BITS("deriveBits");

    private final String opValue;

    JwkOperation(String opValue) {
        this.opValue = opValue;
    }

    @JsonValue
    public String getOpValue() {
        return opValue;
    }
}
