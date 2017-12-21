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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String token = AuthorizationUtils.getAccessTokenFromHeader(authorizationHeader, tokenService);

            Authorization authorizationRequired = resourceMethod.getAnnotation(Authorization.class);

            // Check scope
            Set<String> scopes = Stream.of(tokenService.getTokenClaim(token, "scope").split(" ")).collect(Collectors.toSet());
            List<String> requiredScopes = Arrays.asList(authorizationRequired.scope());
            if (!scopes.contains("privileged") && requiredScopes.size() > 0 && !scopes.containsAll(requiredScopes)) {
                throw new OAuth2Exception(ErrorType.OAuth2.UNAUTHORIZED_SCOPE, Response.Status.FORBIDDEN, String.join(" ", requiredScopes));
            }
        }
    }
}
