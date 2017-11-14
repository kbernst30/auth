package ca.bernstein;

import ca.bernstein.configuration.JpaConfiguration;
import ca.bernstein.factories.hk2.ConfigurationProviderFactory;
import ca.bernstein.factories.hk2.HttpSessionFactory;
import ca.bernstein.factories.hk2.JpaConfigurationFactory;
import ca.bernstein.factories.jose.JwsAlgorithmFactory;
import ca.bernstein.factories.jose.KeyProviderFactory;
import ca.bernstein.filters.AuthenticationFilter;
import ca.bernstein.models.jpa.Account;
import ca.bernstein.models.jpa.AllowedScope;
import ca.bernstein.persistence.*;
import ca.bernstein.persistence.hibernate.HibernateDao;
import ca.bernstein.persistence.hibernate.HibernateSessionProvider;
import ca.bernstein.services.authentication.AuthenticationService;
import ca.bernstein.services.authorization.OAuth2AuthorizationService;
import ca.bernstein.services.jose.JwtTokenService;
import ca.bernstein.services.jose.KeyManager;
import ca.bernstein.services.jose.KeyManagerImpl;
import ca.bernstein.services.jose.TokenService;
import org.cfg4j.provider.ConfigurationProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

public class App extends ResourceConfig {

    public App() {

        // Scan packages to add to Jersey
        packages("ca.bernstein.providers", "ca.bernstein.resources");

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
                bind(AccountDao.class).to(Account.class).in(Singleton.class);
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
                bind(OAuth2AuthorizationService.class).to(OAuth2AuthorizationService.class).in(Singleton.class);

                // Sessions
                bindFactory(HttpSessionFactory.class).to(HttpSession.class);
            }
        });
    }

    private void configureTemplates() {
        property(MvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/templates/");
        property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/resources/(css|js)/.*");
    }
}
