package ca.bernstein.converters;

import ca.bernstein.factories.web.AuthorizationResponseTypeFactoy;
import ca.bernstein.models.common.AuthorizationResponseType;

import javax.ws.rs.ext.ParamConverter;

public class AuthorizationResponseTypeConverter implements ParamConverter<AuthorizationResponseType> {

    private final AuthorizationResponseTypeFactoy authorizationResponseTypeFactoy;

    public AuthorizationResponseTypeConverter(AuthorizationResponseTypeFactoy authorizationResponseTypeFactoy) {
        this.authorizationResponseTypeFactoy = authorizationResponseTypeFactoy;
    }

    @Override
    public AuthorizationResponseType fromString(String responseTypeStr) {
        return authorizationResponseTypeFactoy.createAuthorizationResponseType(responseTypeStr);
    }

    @Override
    public String toString(AuthorizationResponseType authorizationResponseType) {
        return authorizationResponseType.toString();
    }
}
