package io.keystash.common.services.jose;

import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.factories.jose.KeyProviderFactory;
import io.keystash.common.models.jose.HmacKey;
import io.keystash.common.models.jose.RsaKey;
import io.keystash.common.models.jose.SigningKey;
import io.keystash.common.persistence.AppKeyDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of KeyManager
 */
@Slf4j
public class KeyManagerImpl implements KeyManager {

    private final AppKeyDao appKeyDao;
    private final KeyProviderFactory keyProviderFactory;

    @Inject
    public KeyManagerImpl(AppKeyDao appKeyDao, KeyProviderFactory keyProviderFactory) {
        this.appKeyDao = appKeyDao;
        this.keyProviderFactory = keyProviderFactory;
    }

    @Override
    public SigningKey getActiveKey() {
        return getKeyProviders().stream()
                .filter(KeyProvider::isActive)
                .findFirst()
                .map(this::createSigningKey)
                .orElse(null);
    }

    @Override
    public Set<SigningKey> getPassiveKeys() {
        return getKeyProviders().stream()
                .filter(KeyProvider::isPassive)
                .map(this::createSigningKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SigningKey> getDisabledKeys() {
        return getKeyProviders().stream()
                .filter(keyProvider -> !keyProvider.isActive() && !keyProvider.isPassive())
                .map(this::createSigningKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SigningKey> getAllKeys() {
        return getKeyProviders().stream()
                .map(this::createSigningKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private SigningKey createSigningKey(KeyProvider keyProvider) {
        switch (keyProvider.getAlgorithmType()) {
            case HMAC:
                return createHmacKey((HmacSecretKeyProvider) keyProvider);
            case RSA:
                return createRsaKey((RsaKeyProvider) keyProvider);
            default:
                return null;
        }
    }

    private HmacKey createHmacKey(HmacSecretKeyProvider hmacSecretKeyProvider) {
        return new HmacKey(hmacSecretKeyProvider.getKeyId(), hmacSecretKeyProvider.getSecret());
    }

    private RsaKey createRsaKey(RsaKeyProvider rsaKeyProvider) {
        return new RsaKey(rsaKeyProvider.getKeyId(), rsaKeyProvider.getPublicKey(), rsaKeyProvider.getPrivateKey());
    }

    private List<KeyProvider> getKeyProviders() {
        List<KeyProvider> keyProviders = new ArrayList<>();

        try {
            appKeyDao.getKeys().forEach(appKey -> {
                KeyProvider keyProvider = keyProviderFactory.createKeyProvider(appKey);
                if (keyProvider != null) {
                    keyProviders.add(keyProvider);
                }
            });
        } catch (JpaExecutionException e) {
            log.error("Unable to load application keys. Returning an empty list", e);
        }

        return keyProviders;
    }
}
