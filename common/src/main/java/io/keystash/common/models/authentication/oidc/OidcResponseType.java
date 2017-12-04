package io.keystash.common.models.authentication.oidc;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.keystash.common.models.common.AuthorizationResponseType;

/**
 * Represents the valid types of OpenID Connect responses that can be returned to an authorization request
 */
public enum OidcResponseType implements AuthorizationResponseType {

    ID_TOKEN;

    @JsonCreator
    public static OidcResponseType fromString(String responseType) {
        for (OidcResponseType type : values()) {
            if (responseType != null && responseType.toUpperCase().equals(type.name())) {
                return type;
            }
        }

        return null;
    }
}
