package io.keystash.models.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OAuth2GrantType {

    AUTHORIZATION_CODE,
    CLIENT_CREDENTIALS,
    IMPLICIT,
    PASSWORD,
    REFRESH_TOKEN;

    @JsonCreator
    public static OAuth2GrantType fromString(String grantType) {
        for (OAuth2GrantType type : values()) {
            if (grantType != null && grantType.toUpperCase().equals(type.name())) {
                return type;
            }
        }

        return null;
    }

}
