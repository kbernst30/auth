package io.keystash.core.persistence;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.OpenIdProviderConfig;
import io.keystash.common.persistence.JpaEntityDao;

import javax.inject.Inject;
import java.util.List;

public class OpenIdProviderConfigDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public OpenIdProviderConfigDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public List<OpenIdProviderConfig> getOpenIdProviderConfigs() throws JpaExecutionException {
        return this.jpaEntityDao.getEntities(OpenIdProviderConfig.class);
    }
}
