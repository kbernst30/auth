package ca.bernstein.persistence;

import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.jpa.AppKey;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class AppKeyDao {

    private final JpaEntityDao jpaEntityDao;

    @Inject
    public AppKeyDao(JpaEntityDao jpaEntityDao) {
        this.jpaEntityDao = jpaEntityDao;
    }

    public List<AppKey> getKeys() throws JpaExecutionException {
        return jpaEntityDao.getEntities(AppKey.class);
    }
}
