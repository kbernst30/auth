package io.keystash;

import io.keystash.configuration.JpaConfiguration;
import io.keystash.factories.hk2.AuthenticatedUserFactory;
import io.keystash.factories.hk2.ConfigurationProviderFactory;
import io.keystash.factories.hk2.HttpSessionFactory;
import io.keystash.factories.hk2.JpaConfigurationFactory;
import io.keystash.factories.jose.JwsAlgorithmFactory;
import io.keystash.factories.jose.KeyProviderFactory;
import io.keystash.filters.AuthenticationFilter;
import io.keystash.models.authentication.AuthenticatedUser;
import io.keystash.models.jpa.AllowedScope;
import io.keystash.persistence.*;
import io.keystash.persistence.hibernate.HibernateDao;
import io.keystash.persistence.hibernate.HibernateSessionProvider;
import io.keystash.services.authentication.AuthenticationService;
import io.keystash.services.authentication.DefaultUserInfoService;
import io.keystash.services.authentication.UserInfoService;
import io.keystash.services.authorization.AuthorizationService;
import io.keystash.services.jose.JwtTokenService;
import io.keystash.services.jose.KeyManager;
import io.keystash.services.jose.KeyManagerImpl;
import io.keystash.services.jose.TokenService;
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
public class App extends ResourceConfig {

    public App() {

        // Scan packages to add to Jersey
        packages("io.keystash.providers", "io.keystash.resources");

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
                bind(PlatformClientDao.class).to(PlatformClientDao.class).in(Singleton.class);
                bind(ScopeDao.class).to(ScopeDao.class).in(Singleton.class);

                // Factories
                bind(JwsAlgorithmFactory.class).to(JwsAlgorithmFactory.class).in(Singleton.class);
                bind(KeyProviderFactory.class).to(KeyProviderFactory.class).in(Singleton.class);

                // Service Layer
                bind(AuthenticationService.class).to(AuthenticationService.class).in(Singleton.class);
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
