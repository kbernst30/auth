package io.keystash.common.factories.jose;

import io.keystash.common.exceptions.SigningKeyException;
import io.keystash.common.models.jose.JwsAlgorithmType;
import io.keystash.common.models.jose.KeyConfigName;
import io.keystash.common.models.jpa.AppKey;
import io.keystash.common.models.jpa.AppKeyConfig;
import io.keystash.common.services.jose.HmacSecretKeyProvider;
import io.keystash.common.services.jose.KeyProvider;
import io.keystash.common.services.jose.RsaKeyProvider;
import io.keystash.common.util.KeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

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
                keyProvider = buildHmacKeyProvider(appKey.getId(), appKey.getConfigs());
                break;
            case RSA:
                keyProvider = buildRsaKeyProvider(appKey.getId(), appKey.getConfigs());
                break;
            default:
                keyProvider = null;
                break;
        }

        return keyProvider;
    }

    private HmacSecretKeyProvider buildHmacKeyProvider(int keyId, List<AppKeyConfig> appKeyConfigs) {
        boolean isActive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.ACTIVE));
        boolean isPassive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.PASSIVE));
        String kid = UUID.nameUUIDFromBytes(String.valueOf(keyId).getBytes()).toString();
        String secret = getConfigValueByName(appKeyConfigs, KeyConfigName.SECRET);

        return new HmacSecretKeyProvider(kid, secret, isActive, isPassive);
    }

    private RsaKeyProvider buildRsaKeyProvider(int keyId, List<AppKeyConfig> appKeyConfigs) {
        boolean isActive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.ACTIVE));
        boolean isPassive = Boolean.parseBoolean(getConfigValueByName(appKeyConfigs, KeyConfigName.PASSIVE));
        String kid = UUID.nameUUIDFromBytes(String.valueOf(keyId).getBytes()).toString();

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

        return new RsaKeyProvider(kid, (RSAPublicKey) publicKey, (RSAPrivateKey) privateKey, isActive, isPassive);
    }

    private String getConfigValueByName(List<AppKeyConfig> appKeyConfigs, KeyConfigName keyConfigName) {
        return appKeyConfigs.stream()
                .filter(config -> config.getName() != null && keyConfigName.getValue().equals(config.getName()))
                .map(AppKeyConfig::getValue)
                .findFirst()
                .orElse(null);
    }

}
