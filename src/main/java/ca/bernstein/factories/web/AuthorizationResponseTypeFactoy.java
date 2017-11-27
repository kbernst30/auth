package ca.bernstein.factories.web;

import ca.bernstein.models.authentication.oidc.OidcResponseType;
import ca.bernstein.models.common.AuthorizationResponseType;
import ca.bernstein.models.oauth.OAuth2ResponseType;

/**
 * Construct an appropriate response type object from a given parameter string
 */
public class AuthorizationResponseTypeFactoy {

    /**
     * Returns an appropriate response type implementation based on the given string parameter
     * <p>If no appropriate response type can be determined, return null</p>
     * @param responseTypeString the string to construct a response stype from
     * @return a new implementation of {@link AuthorizationResponseType}
     */
    public AuthorizationResponseType createAuthorizationResponseType(String responseTypeString) {
        AuthorizationResponseType responseType = OAuth2ResponseType.fromString(responseTypeString);
        if (responseType == null) {
            responseType = OidcResponseType.fromString(responseTypeString);
        }

        return responseType;
    }

}
