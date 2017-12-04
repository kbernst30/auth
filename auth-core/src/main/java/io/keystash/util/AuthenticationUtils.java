package io.keystash.util;

import io.keystash.common.models.authentication.AuthenticatedUser;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static String getSubjectIdentifierForUser(AuthenticatedUser authenticatedUser) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Hardcoding the algorithm should mean we never get here
            throw new RuntimeException("Unable to generate subject identifier for user", e);
        }

        String salt = authenticatedUser.getEmail() + ":" + authenticatedUser.getUserId(); // TODO more secure/random salt
        byte[] hash = sha256.digest(salt.getBytes());
        return UUID.nameUUIDFromBytes(hash).toString();
    }

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    public static String getHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
