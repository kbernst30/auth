package io.keystash.core;

import io.keystash.common.configuration.JpaConfiguration;
import io.keystash.common.persistence.JpaEntityDao;
import io.keystash.core.factories.hk2.*;
import io.keystash.core.factories.jose.JwkFactory;
import io.keystash.core.factories.jose.JwsAlgorithmFactory;
import io.keystash.core.factories.jose.KeyProviderFactory;
import io.keystash.core.filters.AuthenticationFilter;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.jpa.AllowedScope;
import io.keystash.core.models.web.HostInfo;
import io.keystash.core.persistence.*;
import io.keystash.common.persistence.hibernate.HibernateDao;
import io.keystash.common.persistence.hibernate.HibernateSessionProvider;
import io.keystash.core.services.authentication.*;
import io.keystash.core.services.authorization.AuthorizationService;
import io.keystash.core.services.jose.JwtTokenService;
import io.keystash.core.services.jose.KeyManager;
import io.keystash.core.services.jose.KeyManagerImpl;
import io.keystash.core.services.jose.TokenService;
import org.cfg4j.provider.ConfigurationProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ApplicationPath;


@ApplicationPath("auth")
public class AuthCore extends ResourceConfig {

    public AuthCore() {

        // Scan packages to add to Jersey
        packages(
                "io.keystash.common.providers",
                "io.keystash.core.providers",
                "io.keystash.core.resources"
        );

        // Register necessary dependencies
        register(JacksonFeature.class);
        register(JspMvcFeature.class);

        // Register Filters
        register(AuthenticationFilter.class);

        // DI layer
        configureDependencyInjection();

        // Templates
        configureTemplates();
    }

    private void configureDependencyInjection() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // Configuration Layer
                bindFactory(ConfigurationProviderFactory.class).to(ConfigurationProvider.class).in(Singleton.class);
                bindFactory(JpaConfigurationFactory.class).to(JpaConfiguration.class).in(Singleton.class);

                // Data Layer - TODO use different JPA or perhaps none at all based on config
                bind(HibernateSessionProvider.class).to(HibernateSessionProvider.class).in(Singleton.class);
                bind(HibernateDao.class).to(JpaEntityDao.class).in(Singleton.class);
                bind(AccountDao.class).to(AccountDao.class).in(Singleton.class);
                bind(AllowedScope.class).to(AllowedScope.class).in(Singleton.class);
                bind(AppKeyDao.class).to(AppKeyDao.class).in(Singleton.class);
                bind(OpenIdProviderConfigDao.class).to(OpenIdProviderConfigDao.class).to(Singleton.class);
                bind(PlatformClientDao.class).to(PlatformClientDao.class).in(Singleton.class);
                bind(ScopeDao.class).to(ScopeDao.class).in(Singleton.class);

                // Factories
                bind(JwkFactory.class).to(JwkFactory.class).in(Singleton.class);
                bind(JwsAlgorithmFactory.class).to(JwsAlgorithmFactory.class).in(Singleton.class);
                bind(KeyProviderFactory.class).to(KeyProviderFactory.class).in(Singleton.class);

                // Misc
                bindFactory(HostInfoFactory.class).to(HostInfo.class);

                // Service Layer
                bind(AuthenticationService.class).to(AuthenticationService.class).in(Singleton.class);
                bind(DefaultDiscoveryService.class).to(DiscoveryService.class).in(Singleton.class);
                bind(JwtTokenService.class).to(TokenService.class).in(Singleton.class);
                bind(KeyManagerImpl.class).to(KeyManager.class).in(Singleton.class);
                bind(AuthorizationService.class).to(AuthorizationService.class).in(Singleton.class);
                bind(DefaultUserInfoService.class).to(UserInfoService.class).in(Singleton.class);

                // Sessions
                bindFactory(AuthenticatedUserFactory.class).to(AuthenticatedUser.class);
                bindFactory(HttpSessionFactory.class).to(HttpSession.class);
            }
        });
    }

    private void configureTemplates() {
        property(MvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/templates/");
        property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/resources/(css|js)/.*");
    }
}
