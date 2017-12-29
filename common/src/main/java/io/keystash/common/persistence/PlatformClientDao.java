package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.Client;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Slf4j
public class PlatformClientDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public PlatformClientDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public Client getPlatformClientByClientId(String clientId) throws JpaExecutionException {
        return jpaEntityDao.doWork(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Client> query = criteriaBuilder.createQuery(Client.class);
            Root<Client> platformClientRoot = query.from(Client.class);

            query.select(platformClientRoot).where(criteriaBuilder.equal(platformClientRoot.get("clientId"), clientId));
            List<Client> clientList = session.createQuery(query).getResultList();

            return clientList.size() > 0 ? clientList.get(0) : null;
        });
    }

}
