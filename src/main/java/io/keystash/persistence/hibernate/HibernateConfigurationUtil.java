package io.keystash.persistence.hibernate;

import io.keystash.configuration.JpaConfiguration;
import ca.bernstein.models.jpa.*;
import io.keystash.models.jpa.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for initializing Hibernate ORM connections
 */
@Slf4j
public final class HibernateConfigurationUtil {

    private static SessionFactory sessionFactory;
    private static StandardServiceRegistry registry;

    public static void initialize(JpaConfiguration jpaConfiguration) {
        createSessionFactory(jpaConfiguration);
    }

    public static void close() {
        if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
        if (sessionFactory != null) sessionFactory.close();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static void createSessionFactory(JpaConfiguration jpaConfiguration) {
        if (sessionFactory == null) {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

            Map<String, Object> jpaSettings = new HashMap<>();
            jpaSettings.put(Environment.DRIVER, jpaConfiguration.driver());
            jpaSettings.put(Environment.URL, jpaConfiguration.connnectionUrl());
            jpaSettings.put(Environment.USER, jpaConfiguration.username());
            jpaSettings.put(Environment.PASS, jpaConfiguration.password());
            jpaSettings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect"); // TODO This should be configured
            jpaSettings.put(Environment.SHOW_SQL, false);

            // TODO add configurations for this
            jpaSettings.put(Environment.C3P0_MIN_SIZE, 3);
            jpaSettings.put(Environment.C3P0_MAX_SIZE, 50);
            jpaSettings.put(Environment.C3P0_ACQUIRE_INCREMENT, 5);
            jpaSettings.put(Environment.C3P0_TIMEOUT, 1800);
            jpaSettings.put(Environment.C3P0_IDLE_TEST_PERIOD, 1800);

            jpaSettings.put(Environment.HBM2DDL_AUTO, "update");
            jpaSettings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

            registryBuilder.applySettings(jpaSettings);

            registry = registryBuilder.build();

            MetadataSources entitySources = new MetadataSources(registry)
                    .addAnnotatedClass(Account.class)
                    .addAnnotatedClass(AllowedScope.class)
                    .addAnnotatedClass(AppKey.class)
                    .addAnnotatedClass(AppKeyConfig.class)
                    .addAnnotatedClass(PlatformClient.class)
                    .addAnnotatedClass(RedirectUri.class);

            sessionFactory = entitySources.getMetadataBuilder().build().getSessionFactoryBuilder().build();
        }
    }
}
