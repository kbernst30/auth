package io.keystash.common.models.authentication.oidc;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Specifies how to display authentication and consent UI pages to users
 */
public enum OidcUserInterfaceDisplay {

    PAGE,
    POPUP,
    TOUCH,
    WAP;

    @JsonCreator
    public static OidcUserInterfaceDisplay fromString(String display) {
        for (OidcUserInterfaceDisplay oidcUserInterfaceDisplay : values()) {
            if (display != null && display.toUpperCase().equals(oidcUserInterfaceDisplay.name())) {
                return oidcUserInterfaceDisplay;
            }
        }

        return OidcUserInterfaceDisplay.PAGE;
    }

}
