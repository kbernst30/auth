package io.keystash.admin;

import io.keystash.admin.factories.hk2.ResourceOwnerFactory;
import io.keystash.admin.models.ResourceOwner;
import io.keystash.common.configuration.JpaConfiguration;
import io.keystash.common.factories.hk2.ConfigurationProviderFactory;
import io.keystash.common.factories.hk2.JpaConfigurationFactory;
import io.keystash.common.factories.jose.JwkFactory;
import io.keystash.common.factories.jose.JwsAlgorithmFactory;
import io.keystash.common.factories.jose.KeyProviderFactory;
import io.keystash.common.persistence.AppKeyDao;
import io.keystash.common.persistence.JpaEntityDao;
import io.keystash.common.persistence.ClientDao;
import io.keystash.common.persistence.ScopeDao;
import io.keystash.common.persistence.hibernate.HibernateDao;
import io.keystash.common.persistence.hibernate.HibernateSessionProvider;
import io.keystash.common.services.jose.JwtTokenService;
import io.keystash.common.services.jose.KeyManager;
import io.keystash.common.services.jose.KeyManagerImpl;
import io.keystash.common.services.jose.TokenService;
import org.cfg4j.provider.ConfigurationProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

public class AdminConsole extends ResourceConfig {

    public AdminConsole() {
        packages(
                "io.keystash.admin.filters",
                "io.keystash.admin.providers",
                "io.keystash.admin.resources",
                "io.keystash.common.providers"
        );

        register(JacksonFeature.class);

        configureDependencyInjection();
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
                bind(AppKeyDao.class).to(AppKeyDao.class).in(Singleton.class);
                bind(ClientDao.class).to(ClientDao.class).in(Singleton.class);
                bind(ScopeDao.class).to(ScopeDao.class).in(Singleton.class);

                // Factories
                bind(JwkFactory.class).to(JwkFactory.class).in(Singleton.class);
                bind(JwsAlgorithmFactory.class).to(JwsAlgorithmFactory.class).in(Singleton.class);
                bind(KeyProviderFactory.class).to(KeyProviderFactory.class).in(Singleton.class);

                // Misc
                bindFactory(ResourceOwnerFactory.class).to(ResourceOwner.class);

                // Services
                bind(JwtTokenService.class).to(TokenService.class).in(Singleton.class);
                bind(KeyManagerImpl.class).to(KeyManager.class).in(Singleton.class);
            }
        });
    }
}
