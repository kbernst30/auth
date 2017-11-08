package ca.bernstein.models.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents the valid types of responses that can be returned to an authorization request
 */
public enum OAuth2ResponseType {

    CODE,
    TOKEN;

    @JsonCreator
    public static OAuth2ResponseType fromString(String responseType) {
        for (OAuth2ResponseType type : values()) {
            if (responseType != null && responseType.toUpperCase().equals(type.name())) {
                return type;
            }
        }

        return null;
    }
}
