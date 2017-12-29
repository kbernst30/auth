package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.Account;
import io.keystash.common.models.jpa.User;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class UserDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public UserDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public User getUserById(int id) throws JpaExecutionException {
        return jpaEntityDao.getEntityById(User.class, id);
    }

    public User getApplicationUserByUsername(int applicationId, String username) throws JpaExecutionException {
        return jpaEntityDao.doWork(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            Root<User> userRoot = query.from(User.class);

            query.select(userRoot)
                    .where(criteriaBuilder.and(
                            criteriaBuilder.equal(userRoot.get("username"), username),
                            criteriaBuilder.equal(userRoot.get("application.id"), applicationId)));

            List<User> userList = session.createQuery(query).getResultList();
            return userList.size() > 0 ? userList.get(0) : null;
        });
    }
}
