package ca.bernstein.models.jose;

import lombok.Getter;

public enum JwtReservedClaim {

    EXPIRY_TIME("exp"),
    NOT_BEFORE_TIME("nbf"),
    ISSUED_AT("iat"),
    ISSUER("iss"),
    AUDIENCE("aud"),
    PRINCIPAL("prn"),
    JTI("jti"),
    TYPE("typ");

    @Getter private final String value;

    JwtReservedClaim(String value) {
        this.value = value;
    }

    public static JwtReservedClaim fromString(String claim) {
        for (JwtReservedClaim reservedClaim : values()) {
            if (reservedClaim.getValue().equals(claim)) {
                return reservedClaim;
            }
        }

        return null;
    }
}
