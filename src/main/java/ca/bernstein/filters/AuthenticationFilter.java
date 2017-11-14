package ca.bernstein.filters;

import ca.bernstein.annotation.AuthenticationRequired;
import ca.bernstein.util.AuthenticationUtils;

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

public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private Provider<HttpSession> httpSessionProvider;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();
        HttpSession session = httpSessionProvider.get();

        if (resourceMethod.isAnnotationPresent(AuthenticationRequired.class) && !AuthenticationUtils.isValidSession(session)) {
            containerRequestContext.abortWith(Response.temporaryRedirect(URI.create("/auth/login")).build());
        }
    }

}
