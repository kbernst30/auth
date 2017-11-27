package ca.bernstein.util;

import ca.bernstein.models.authentication.AuthenticatedUser;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.http.HttpSession;

public class AuthenticationUtils {

    public static final String OPEN_ID_SCOPE = "openid";
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

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    public static String getHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
