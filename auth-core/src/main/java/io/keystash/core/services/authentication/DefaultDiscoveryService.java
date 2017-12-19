package io.keystash.core.services.authentication;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.authentication.oidc.OidcResponseType;
import io.keystash.common.models.jpa.OpenIdProviderConfig;
import io.keystash.common.models.oauth.OAuth2GrantType;
import io.keystash.common.models.oauth.OAuth2ResponseType;
import io.keystash.core.models.authentication.OpenIdProviderMetadata;
import io.keystash.core.models.web.HostInfo;
import io.keystash.core.persistence.OpenIdProviderConfigDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // TODO add in the user specified configurations

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
        openIdProviderMetadata.setJwksUri(hostInfo.getBaseUrl() + "jwks");

        // For now only support RSA key pairs as valid signing
        openIdProviderMetadata.setIdTokenEncryptionAlgValuesSupported(Stream.of("RS256").collect(Collectors.toSet()));
        openIdProviderMetadata.setSubjectTypesSupported(Stream.of("pairwise", "public").collect(Collectors.toSet()));
        openIdProviderMetadata.setResponseTypesSupported(getSupportedResponseTypes());
        openIdProviderMetadata.setGrantTypesSupported(Stream.of(OAuth2GrantType.values())
                .map(OAuth2GrantType::name)
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));

        return openIdProviderMetadata;
    }

    private Set<String> getSupportedResponseTypes() {
        Set<String> supportedResponseTypes = new HashSet<>();

        supportedResponseTypes.addAll(Arrays.stream(OAuth2ResponseType.values())
                .map(OAuth2ResponseType::name)
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));

        supportedResponseTypes.addAll(Arrays.stream(OidcResponseType.values())
                .map(OidcResponseType::name)
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));

        // Add the combined Open ID Connect response types
        String codeTokenResponse = OAuth2ResponseType.CODE.name() + " " + OAuth2ResponseType.TOKEN.name();
        String codeIdTokenResponse = OAuth2ResponseType.CODE.name() + " " + OidcResponseType.ID_TOKEN.name();
        String idTokenTokenResponse = OidcResponseType.ID_TOKEN.name() + " " + OAuth2ResponseType.TOKEN.name();
        String codeIdTokenTokenResponse = OAuth2ResponseType.CODE.name() + " " + OidcResponseType.ID_TOKEN.name() +
                " " + OAuth2ResponseType.TOKEN.name();

        supportedResponseTypes.add(codeTokenResponse.toLowerCase());
        supportedResponseTypes.add(codeIdTokenResponse.toLowerCase());
        supportedResponseTypes.add(idTokenTokenResponse.toLowerCase());
        supportedResponseTypes.add(codeIdTokenTokenResponse.toLowerCase());

        return supportedResponseTypes;
    }
}
