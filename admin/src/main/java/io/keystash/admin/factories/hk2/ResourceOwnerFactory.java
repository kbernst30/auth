package io.keystash.admin.factories.hk2;

import io.keystash.admin.models.ResourceOwner;
import io.keystash.common.services.jose.TokenService;
import io.keystash.common.util.AuthorizationUtils;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

public class ResourceOwnerFactory implements Factory<ResourceOwner> {

    private final HttpServletRequest request;
    private final TokenService tokenService;

    @Inject
    public ResourceOwnerFactory(Provider<HttpServletRequest> requestProvider, TokenService tokenService) {
        this.request = requestProvider.get();
        this.tokenService = tokenService;
    }

    @Override
    public ResourceOwner provide() {
        String token = AuthorizationUtils.getAccessTokenFromHeader(request.getHeader(HttpHeaders.AUTHORIZATION), tokenService);
        String idStr = tokenService.getTokenClaim(token, "account_id");
        return new ResourceOwner(Integer.parseInt(idStr));
    }

    @Override
    public void dispose(ResourceOwner resourceOwner) {}
}
