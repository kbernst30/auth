package io.keystash.common.persistence.hibernate;

import io.keystash.common.configuration.JpaConfiguration;
import io.keystash.common.exceptions.jpa.JpaInitializationException;
import io.keystash.common.persistence.JpaEntityManagerProvider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of JpaEntityManagerProvider that returns Hibernate's Session
 */
@Singleton
public class HibernateSessionProvider implements JpaEntityManagerProvider {

    @Inject
    public HibernateSessionProvider(JpaConfiguration jpaConfiguration) {
        HibernateConfigurationUtil.initialize(jpaConfiguration);
    }

    @Override
    public Session getEntityManager() throws JpaInitializationException {
        SessionFactory sessionFactory = HibernateConfigurationUtil.getSessionFactory();
        if (sessionFactory == null) {
            throw new JpaInitializationException("Session factory failed to initialize properly");
        }

        return sessionFactory.getCurrentSession();
    }
}
