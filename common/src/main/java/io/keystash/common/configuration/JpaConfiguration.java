package io.keystash.common.configuration;

import io.keystash.common.annotation.ConfigFile;

import javax.inject.Singleton;

/**
 * An object containing configuration for JPA integration
 */
@Singleton
@ConfigFile("jpaConfiguration.properties")
public interface JpaConfiguration extends Configuration {

    String namespace = "jpaConfiguration";

    /**
     * Gets the connection URL provided in the configuration file
     * @return the connection URL string
     */
    String connnectionUrl();

    /**
     * Gets the username credential provided in the configuration file
     * @return the username for the connection
     */
    String username();

    /**
     * Gets the password credential provided in the configuration file
     * @return the password for the connection
     */
    String password();

    /**
     * Gets the requested JDBC driver
     * @return the JDBC driver class
     */
    String driver();
}
