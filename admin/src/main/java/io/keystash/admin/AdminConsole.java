package io.keystash.admin;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class AdminConsole extends ResourceConfig {

    public AdminConsole() {
        packages(
                "io.keystash.admin.providers",
                "io.keystash.admin.resources",
                "io.keystash.common.providers"
        );

        register(JacksonFeature.class);

        configureDependencyInjection();
    }

    private void configureDependencyInjection() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {

            }
        });
    }
}
