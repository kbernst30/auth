package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaInitializationException;

import javax.persistence.EntityManager;

/**
 * Provides an appropriate JPA entity manager to be used for persistence transactions
 */
public interface JpaEntityManagerProvider {

    /**
     * Get an appropriate entity manager implentation
     * @return an instance of JPA EntityManager
     */
    EntityManager getEntityManager() throws JpaInitializationException;

}
