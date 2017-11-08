package ca.bernstein.factories.hk2;

import ca.bernstein.configuration.JpaConfiguration;
import org.cfg4j.provider.ConfigurationProvider;

import javax.inject.Inject;

public class JpaConfigurationFactory extends ConfigurationFactory<JpaConfiguration> {

    @Inject
    public JpaConfigurationFactory(ConfigurationProvider configurationProvider) {
        super(configurationProvider, JpaConfiguration.namespace, JpaConfiguration.class);
    }

}