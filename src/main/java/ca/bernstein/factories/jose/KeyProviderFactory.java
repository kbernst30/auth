package ca.bernstein.factories.jose;

import ca.bernstein.models.jose.JwsAlgorithmType;
import ca.bernstein.models.jose.KeyConfigName;
import ca.bernstein.models.jpa.AppKey;
import ca.bernstein.models.jpa.AppKeyConfig;
import ca.bernstein.services.jose.HmacSecretKeyProvider;
import ca.bernstein.services.jose.KeyProvider;
import ca.bernstein.services.jose.RsaKeyProvider;

import java.util.List;

/**
 * Constructs an appropriate key provider based on the algorithm used by the key
 */
public class KeyProviderFactory {

    /**
     * Create an appropriate key provider from the persistent app key item
     * @param appKey the app key entity
     * @return a new key provider implementation
     */
    public KeyProvider createKeyProvider(AppKey appKey) {
        KeyProvider keyProvider;

        JwsAlgorithmType algorithmType = JwsAlgorithmType.valueOf(appKey.getAlgorithm().toUpperCase());
        switch (algorithmType) {
            case HMAC:
                keyProvider = buildHmacKeyProvider(appKey.getConfigs());
                break;
            case RSA:
                keyProvider = buildRsaKeyProvider(appKey.getConfigs());
                break;
            default:
                keyProvider = null;
                break;
        }

        return keyProvider;
    }

    private HmacSecretKeyProvider buildHmacKeyProvider(List<AppKeyConfig> appKeyConfigs) {
        boolean isActive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.ACTIVE));
        boolean isPassive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.PASSIVE));
        String secret = getConfigValueByName(appKeyConfigs, KeyConfigName.SECRET);

        return new HmacSecretKeyProvider(secret, isActive, isPassive);
    }

    private RsaKeyProvider buildRsaKeyProvider(List<AppKeyConfig> appKeyConfigs) {
        boolean isActive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.ACTIVE));
        boolean isPassive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.PASSIVE));

        // TODO get the keys from the value in config
        return new RsaKeyProvider(null, null, isActive, isPassive);
    }

    private String getConfigValueByName(List<AppKeyConfig> appKeyConfigs, KeyConfigName keyConfigName) {
        return appKeyConfigs.stream()
                .filter(config -> config.getName() != null && keyConfigName.getValue().equals(config.getName()))
                .map(AppKeyConfig::getValue)
                .findFirst()
                .orElse(null);
    }

}
