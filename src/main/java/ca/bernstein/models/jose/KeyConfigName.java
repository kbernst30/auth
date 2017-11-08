package ca.bernstein.models.jose;

import lombok.Getter;

public enum KeyConfigName {

    PUBLIC_KEY("publicKey"),
    PRIVATE_KEY("privateKey"),
    SECRET("secret"),
    ACTIVE("active"),
    PASSIVE("passive");

    @Getter private final String value;

    KeyConfigName(String value) {
        this.value = value;
    }
}
