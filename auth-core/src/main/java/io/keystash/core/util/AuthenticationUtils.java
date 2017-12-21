package io.keystash.core.util;

import io.keystash.common.models.authentication.AuthenticatedUser;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class AuthenticationUtils {

    public static final String AUTHENTICATED_USER = "AUTHENTICATED_USER";

    public static boolean isValidSession(HttpSession session) {
        return !session.isNew() && session.getAttribute(AuthenticationUtils.AUTHENTICATED_USER) != null;
    }

    public static AuthenticatedUser getUserFromSession(HttpSession session) {
        if (isValidSession(session)) {
            return (AuthenticatedUser) session.getAttribute(AuthenticationUtils.AUTHENTICATED_USER);
        }

        return null;
    }

    public static String getSessionState(String client, String originUri, String sessionId) {
        String salt = BCrypt.gensalt();
        return getSessionStateWithSalt(client + " " + originUri + " " + sessionId + " " + salt, salt);
    }

    public static String getSessionState(String client, String originUri, String sessionId, String previousState) {
        String[] stateParts = previousState.split("\\.");
        String salt = stateParts.length == 2 ? new String(Base64.getDecoder().decode(stateParts[1].getBytes())) : BCrypt.gensalt();
        return getSessionStateWithSalt(client + " " + originUri + " " + sessionId + " " + salt, salt);
    }

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    public static String getHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private static String getSessionStateWithSalt(String stateInfo, String salt) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");

            return new String(Base64.getEncoder().encode(sha256.digest(stateInfo.getBytes("UTF-8")))) + "." +
                    new String(Base64.getEncoder().encode(salt.getBytes("UTF-8")));

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to hash source using RSA256", e);
        }
    }
}
