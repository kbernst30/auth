package io.keystash.common.persistence.hibernate;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.persistence.JpaEntityDao;
import io.keystash.common.persistence.JpaEntityDao;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;

/**
 * An implementation of JpaEntityDao using Hibernate ORM
 */
@Slf4j
public class HibernateDao implements JpaEntityDao {

    private final HibernateSessionProvider hibernateSessionProvider;

    @Inject
    public HibernateDao(HibernateSessionProvider hibernateSessionProvider) {
        this.hibernateSessionProvider = hibernateSessionProvider;
    }

    @Override
    public <T, K extends Serializable> T getEntityById(Class<T> clazz, K identifier) throws JpaExecutionException {
        return doWork(session -> ((Session) session).get(clazz, identifier));
    }

    @Override
    public <T> void saveEntities(Collection<T> entities) throws JpaExecutionException {
        doWork(session -> {
            entities.forEach(((Session) session)::save);
            return null;
        });
    }

    @Override
    public <T> void updateEntity(T entity) throws JpaExecutionException {
        doWork(session -> {
            ((Session) session).update(entity);
            return null;
        });
    }

    @Override
    public <T> void deleteEntity(T entity) throws JpaExecutionException {
        doWork(session -> {
            ((Session) session).delete(entity);
            return null;
        });
    }

    @Override
    public <T> T doWork(Work<T> work) throws JpaExecutionException {
        Session session = null;
        Transaction transaction = null;
        boolean isNestedTransaction = true;

        try {
            session = this.hibernateSessionProvider.getEntityManager();
            transaction = session.getTransaction();
            if (transaction == null || !transaction.isActive()) {
                transaction = session.beginTransaction();
                isNestedTransaction = false;
            }

            T result = work.execute(session);
            transaction.commit();

            return result;

        } catch (Exception e) {
            if (!isNestedTransaction && transaction != null) {
                transaction.rollback();
            }

            throw new JpaExecutionException(e);
        }
    }
}
