package io.keystash.services.authentication;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.jpa.OpenIdProviderConfig;
import io.keystash.models.authentication.OpenIdProviderMetadata;
import io.keystash.models.web.HostInfo;
import io.keystash.persistence.OpenIdProviderConfigDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.List;

@Slf4j
public class DefaultDiscoveryService implements DiscoveryService {

    private final Provider<HostInfo> hostInfoProvider;
    private final OpenIdProviderConfigDao openIdProviderConfigDao;

    @Inject
    public DefaultDiscoveryService(Provider<HostInfo> hostInfoProvider, OpenIdProviderConfigDao openIdProviderConfigDao) {
        this.hostInfoProvider = hostInfoProvider;
        this.openIdProviderConfigDao = openIdProviderConfigDao;
    }

    @Override
    public OpenIdProviderMetadata discoverOpenIdConfiguration() {
        List<OpenIdProviderConfig> openIdProviderConfigs = getOpenIdProviderConfigs();
        return buildOpenIdProviderMetadata();
    }

    private List<OpenIdProviderConfig> getOpenIdProviderConfigs() {
        try {
            return openIdProviderConfigDao.getOpenIdProviderConfigs();
        } catch (JpaExecutionException e) {
            log.error("There was an unexpected error retrieving configuration for the OpenID Provider", e);
            return Collections.emptyList();
        }
    }

    private OpenIdProviderMetadata buildOpenIdProviderMetadata() {
        HostInfo hostInfo = hostInfoProvider.get();

        OpenIdProviderMetadata openIdProviderMetadata = new OpenIdProviderMetadata();
        openIdProviderMetadata.setIssuer(hostInfo.getBaseUrl());
        openIdProviderMetadata.setAuthorizationEndpoint(hostInfo.getBaseUrl() + "oauth/authorize");
        openIdProviderMetadata.setTokenEndpoint(hostInfo.getBaseUrl() + "oauth/token");
        openIdProviderMetadata.setUserInfoEndpoint(hostInfo.getBaseUrl() + "userinfo");
        openIdProviderMetadata.setJwksUri(hostInfo.getBaseUrl()); // TODO real

        return openIdProviderMetadata;
    }
}
