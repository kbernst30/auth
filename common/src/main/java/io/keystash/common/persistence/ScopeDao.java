package io.keystash.common.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.ApplicationScope;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class ScopeDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public ScopeDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public List<ApplicationScope> getAllowedScopes() throws JpaExecutionException {
        return jpaEntityDao.getEntities(ApplicationScope.class);
    }
}
