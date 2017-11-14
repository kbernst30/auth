package ca.bernstein.filters;

import ca.bernstein.annotation.AuthenticationRequired;
import ca.bernstein.util.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

@Slf4j
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private Provider<HttpSession> httpSessionProvider;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();
        HttpSession session = httpSessionProvider.get();
        URI requestedUri = ((ContainerRequest) containerRequestContext).getRequestUri();

        if (resourceMethod.isAnnotationPresent(AuthenticationRequired.class) && !AuthenticationUtils.isValidSession(session)) {
            containerRequestContext.abortWith(Response.temporaryRedirect(buildLoginUri(requestedUri)).build());
        }
    }

    private URI buildLoginUri(URI requestedUri) {
        try {
            return URI.create("/auth/login?returnTo=" + (new URLCodec()).encode(requestedUri.toString()));
        } catch (EncoderException e) {
            log.warn("Failed to encode returnTo URI [{}]", requestedUri);
            return URI.create("/auth/login");
        }
    }

}
