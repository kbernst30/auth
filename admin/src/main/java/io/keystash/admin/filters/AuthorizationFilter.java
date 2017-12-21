package io.keystash.admin.filters;

import io.keystash.admin.annotation.Authorization;
import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.models.error.ErrorType;
import io.keystash.common.services.jose.TokenService;
import io.keystash.common.util.AuthorizationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private final TokenService tokenService;

    @Inject
    public AuthorizationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();

        if (resourceMethod.isAnnotationPresent(Authorization.class)) {
            String authorizationHeader = ((ContainerRequest) containerRequestContext).getHeaderString(HttpHeaders.AUTHORIZATION);
            String token = AuthorizationUtils.getAccessTokenFromHeader(authorizationHeader, tokenService);
            System.out.println(token);
        }
    }
}
