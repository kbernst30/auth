package io.keystash.core.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.Account;
import io.keystash.common.persistence.JpaEntityDao;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AccountDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public AccountDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public Account getAccountById(int id) throws JpaExecutionException {
        return jpaEntityDao.getEntityById(Account.class, id);
    }

    public Account getAccountByEmail(String email) throws JpaExecutionException {
        return jpaEntityDao.doWork(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Account> query = criteriaBuilder.createQuery(Account.class);
            Root<Account> accountRoot = query.from(Account.class);

            query.select(accountRoot).where(criteriaBuilder.equal(accountRoot.get("email"), email));
            List<Account> accountList = session.createQuery(query).getResultList();

            return accountList.size() > 0 ? accountList.get(0) : null;
        });
    }
}
