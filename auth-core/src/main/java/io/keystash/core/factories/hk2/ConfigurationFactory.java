package io.keystash.core.factories.hk2;

import io.keystash.common.configuration.Configuration;
import org.cfg4j.provider.ConfigurationProvider;
import org.glassfish.hk2.api.Factory;

public abstract class ConfigurationFactory<T extends Configuration> implements Factory<T> {

    private final ConfigurationProvider configurationProvider;
    private final String namespace;
    private final Class<T> type;

    protected ConfigurationFactory(ConfigurationProvider configurationProvider, String namespace, Class<T> type) {
        this.configurationProvider = configurationProvider;
        this.namespace = namespace;
        this.type = type;
    }

    @Override
    public T provide() {
        return this.configurationProvider.bind(this.namespace, this.type);
    }

    @Override
    public void dispose(T t) {}
}
