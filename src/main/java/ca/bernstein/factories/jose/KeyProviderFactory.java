package ca.bernstein.factories.jose;

import ca.bernstein.exceptions.authorization.SigningKeyException;
import ca.bernstein.models.jose.JwsAlgorithmType;
import ca.bernstein.models.jose.KeyConfigName;
import ca.bernstein.models.jpa.AppKey;
import ca.bernstein.models.jpa.AppKeyConfig;
import ca.bernstein.services.jose.HmacSecretKeyProvider;
import ca.bernstein.services.jose.KeyProvider;
import ca.bernstein.services.jose.RsaKeyProvider;
import ca.bernstein.util.KeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Constructs an appropriate key provider based on the algorithm used by the key
 */
@Slf4j
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

        PrivateKey privateKey = null;
        PublicKey publicKey = null;

        try {
            privateKey = KeyUtils.getPrivateKeyFromString(getConfigValueByName(appKeyConfigs, KeyConfigName.PRIVATE_KEY));
            if (!StringUtils.isEmpty(getConfigValueByName(appKeyConfigs, KeyConfigName.PUBLIC_KEY))) {
                publicKey = KeyUtils.getPublicKeyFromString(getConfigValueByName(appKeyConfigs, KeyConfigName.PUBLIC_KEY));
            } else {
                publicKey = KeyUtils.getPublicKeyFromPrivateKey(privateKey);
            }
        } catch (SigningKeyException e) {
            log.warn("Failed to get private or public key... RSA signing of tokens using this key will likely fail.", e);
        }

        return new RsaKeyProvider((RSAPublicKey) publicKey, (RSAPrivateKey) privateKey, isActive, isPassive);
    }

    private String getConfigValueByName(List<AppKeyConfig> appKeyConfigs, KeyConfigName keyConfigName) {
        return appKeyConfigs.stream()
                .filter(config -> config.getName() != null && keyConfigName.getValue().equals(config.getName()))
                .map(AppKeyConfig::getValue)
                .findFirst()
                .orElse(null);
    }

}
