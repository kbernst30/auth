package io.keystash.models.authentication.oidc;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Specifies whether or not an end user should be prompted for reauthentication and consent
 */
public enum OidcPrompt {

    NONE,
    LOGIN,
    CONSENT,
    SELECT_ACCOUNT;

    @JsonCreator
    public static OidcPrompt fromString(String prompt) {
        for (OidcPrompt oidcPrompt : values()) {
            if (prompt != null && prompt.toUpperCase().equals(oidcPrompt.name())) {
                return oidcPrompt;
            }
        }

        return null;
    }
}
