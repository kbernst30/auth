package io.keystash.common.models.authentication.oidc;

import lombok.Getter;

public enum OidcScope {

    OPEN_ID_SCOPE("openid");

    @Getter private final String value;

    OidcScope(String value) {
        this.value = value;
    }

}
