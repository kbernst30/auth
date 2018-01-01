package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.Application;
import io.keystash.common.models.jpa.Client;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Slf4j
public class ClientDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public ClientDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public Client getApplicationClientByClientId(Application application, String clientId) throws JpaExecutionException {
        return jpaEntityDao.doWork(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Client> query = criteriaBuilder.createQuery(Client.class);
            Root<Client> clientRoot = query.from(Client.class);

            query.select(clientRoot)
                    .where(criteriaBuilder.and(
                            criteriaBuilder.equal(clientRoot.get("clientId"), clientId)),
                            criteriaBuilder.equal(clientRoot.get("application"), application));

            List<Client> clientList = session.createQuery(query).getResultList();
            return clientList.size() > 0 ? clientList.get(0) : null;
        });
    }

}
