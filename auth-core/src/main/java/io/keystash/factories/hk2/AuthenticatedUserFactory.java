package io.keystash.factories.hk2;

import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.util.AuthenticationUtils;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

public class AuthenticatedUserFactory implements Factory<AuthenticatedUser> {

    private final HttpSession httpSession;

    @Inject
    public AuthenticatedUserFactory(Provider<HttpSession> httpSessionProvider) {
        this.httpSession = httpSessionProvider.get();
    }

    @Override
    public AuthenticatedUser provide() {
        if (httpSession != null && !httpSession.isNew()) {
            return (AuthenticatedUser) httpSession.getAttribute(AuthenticationUtils.AUTHENTICATED_USER);
        }

        return null;
    }

    @Override
    public void dispose(AuthenticatedUser authenticatedUser) {}
}
