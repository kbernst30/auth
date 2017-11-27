package ca.bernstein.providers;

import ca.bernstein.converters.AuthorizationResponseTypeConverter;
import ca.bernstein.factories.web.AuthorizationResponseTypeFactoy;
import ca.bernstein.models.common.AuthorizationResponseType;

import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class AuthParameterConverterProvider implements ParamConverterProvider {

    private final AuthorizationResponseTypeConverter authorizationResponseTypeConverter;

    @Inject
    public AuthParameterConverterProvider(AuthorizationResponseTypeFactoy authorizationResponseTypeFactoy) {
        this.authorizationResponseTypeConverter = new AuthorizationResponseTypeConverter(authorizationResponseTypeFactoy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(AuthorizationResponseType.class)) {
            return (ParamConverter<T>) authorizationResponseTypeConverter;
        }

        return null;
    }
}
