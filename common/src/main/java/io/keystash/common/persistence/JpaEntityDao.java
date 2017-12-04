package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Data access object that uses JPA to persist entities
 */
public interface JpaEntityDao {

    /**
     * Gets a persistent entity by its unique identifier
     * @param clazz The type of entity being fetched
     * @param identifier the unique identifier object
     * @return An instance of requested type identified by the identifier
     * @throws JpaExecutionException
     */
    <T, K extends Serializable> T getEntityById(Class<T> clazz, K identifier) throws JpaExecutionException;

    /**
     * Get a list of entities of a given type
     * @param clazz The type of entity being fetched
     * @return A list of entities of requested type
     * @throws JpaExecutionException
     */
    default <T> List<T> getEntities(Class<T> clazz) throws JpaExecutionException {
        return doWork(entityManager -> {
            CriteriaQuery<T> query = entityManager.getCriteriaBuilder().createQuery(clazz);
            query.select(query.from(clazz));
            return entityManager.createQuery(query).getResultList();
        });
    }

    /**
     * Persist an entity with the underlying entity manager
     * @param entity The entity to be persisted
     * @throws JpaExecutionException
     */
    default <T> void saveEntity(T entity) throws JpaExecutionException {
        this.saveEntities(Arrays.asList(entity));
    }

    /**
     * Persist a collection of entities with the underlying entity manager
     * @param entities A collection of entities to be persisted
     * @throws JpaExecutionException
     */
    <T> void saveEntities(Collection<T> entities) throws JpaExecutionException;


    /**
     * Updates an entity with the underlying entity manager
     * @param entity The entity to be persisted
     * @throws JpaExecutionException
     */
    <T> void updateEntity(T entity) throws JpaExecutionException;


    /**
     * Delete an entity from the underlying entity manager
     * @param entity The entity to be deleted
     * @throws JpaExecutionException
     */
    <T> void deleteEntity(T entity) throws JpaExecutionException;

    /**
     * Execute an instance of JPA work
     * @param work an instance of Work
     * @return an instance of the type we want returned from the work execution
     * @throws JpaExecutionException
     */
    <T> T doWork(Work<T> work) throws JpaExecutionException;

    /**
     * Functional interface for execution unit
     */
    interface Work<T> {
        /**
         * Execute a unit of work on the entity manager
         * @param entityManager an underlying JPA entity manager to interface with
         * @return The entity or entities being worked on
         * @throws JpaExecutionException
         */
        T execute(EntityManager entityManager) throws JpaExecutionException;
    }
}
