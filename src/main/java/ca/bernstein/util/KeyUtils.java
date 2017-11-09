package ca.bernstein.util;

import ca.bernstein.exceptions.authorization.SigningKeyException;
import ca.bernstein.models.jose.JwsAlgorithmType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public final class KeyUtils {

    public static PrivateKey getPrivateKeyFromString(String keyStr) throws SigningKeyException {
        if (StringUtils.isEmpty(keyStr)) {
            throw new SigningKeyException("Cannot get private key from null or empty string");
        }

        String cleanKeyStr = cleanKeyString(keyStr);
        try {
            byte[] privateBytes = Base64.getDecoder().decode(cleanKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(JwsAlgorithmType.RSA.name());
            return keyFactory.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SigningKeyException("Invalid key string or algorithm was used to generate a private key", e);
        }
    }

    public static PublicKey getPublicKeyFromString(String keyStr) throws SigningKeyException {
        if (StringUtils.isEmpty(keyStr)) {
            throw new SigningKeyException("Cannot get public key from null or empty string");
        }

        String cleanKeyStr = cleanKeyString(keyStr);
        try {
            byte[] publicBytes = Base64.getDecoder().decode(cleanKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(JwsAlgorithmType.RSA.name());
            return keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SigningKeyException("Invalid key string or algorithm was used to generate a public key", e);
        }
    }

    public static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) throws SigningKeyException {
        if (privateKey == null) {
            throw new SigningKeyException("Cannot get public key from null private key");
        }

        try {
            RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance(JwsAlgorithmType.RSA.name());
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SigningKeyException("Could not get the public key fom the provided private key", e);
        }
    }

    private static String cleanKeyString(String keyStr) {
        keyStr = keyStr.replaceAll("-----BEGIN (.*)-----", "");
        keyStr = keyStr.replaceAll("-----END (.*)----", "");
        keyStr = keyStr.replaceAll("\r\n", "");
        keyStr = keyStr.replaceAll("\n", "");
        return keyStr.trim();
    }
}