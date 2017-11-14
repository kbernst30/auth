package ca.bernstein.util;

import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.http.HttpSession;

public class AuthenticationUtils {

    public static final String AUTHENTICATED_USER = "AUTHENTICATED_USER";

    public static boolean isValidSession(HttpSession session) {
        return !session.isNew() && session.getAttribute(AuthenticationUtils.AUTHENTICATED_USER) != null;
    }

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    public static String getHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
