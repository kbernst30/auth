package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.Application;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class ApplicationDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public ApplicationDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public Application getApplicationForHostName(String hostName) throws JpaExecutionException {
        return jpaEntityDao.doWork(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Application> query = criteriaBuilder.createQuery(Application.class);
            Root<Application> applicationRoot = query.from(Application.class);

            query.select(applicationRoot).where(criteriaBuilder.equal(applicationRoot.get("hostName"), hostName));

            List<Application> applicationList = session.createQuery(query).getResultList();
            return applicationList.size() > 0 ? applicationList.get(0) : null;
        });
    }
}
