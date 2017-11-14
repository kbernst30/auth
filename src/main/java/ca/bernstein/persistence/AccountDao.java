package ca.bernstein.persistence;

import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.jpa.Account;

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
