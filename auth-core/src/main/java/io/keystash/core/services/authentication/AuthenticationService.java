package io.keystash.core.services.authentication;

import io.keystash.common.models.jpa.Application;
import io.keystash.common.models.jpa.User;
import io.keystash.common.persistence.ApplicationDao;
import io.keystash.core.exceptions.authentication.AuthenticationException;
import io.keystash.core.exceptions.authentication.InvalidCredentialsException;
import io.keystash.core.exceptions.authentication.UnknownUserException;
import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.core.models.authentication.LoginRequest;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.persistence.UserDao;
import io.keystash.core.util.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

@Slf4j
public class AuthenticationService {

    private final ApplicationDao applicationDao;
    private final UserDao userDao;
    private final Provider<HttpSession> httpSessionProvider;

    @Inject
    public AuthenticationService(ApplicationDao applicationDao, UserDao userDao, Provider<HttpSession> httpSessionProvider) {
        this.applicationDao = applicationDao;
        this.userDao = userDao;
        this.httpSessionProvider = httpSessionProvider;
    }

    @Transactional
    public void login(LoginRequest loginRequest, String loginHost) throws AuthenticationException {

        // TODO support social logins

        HttpSession httpSession = httpSessionProvider.get();

        // Set new session attributes
        AuthenticatedUser authenticatedUser = authenticateAndGetUser(loginRequest.getUsername(), loginRequest.getPassword(),
                loginHost);
        httpSession.setAttribute(AuthenticationUtils.AUTHENTICATED_USER, authenticatedUser);
    }

    @Transactional
    public AuthenticatedUser authenticateAndGetUser(String username, String password, String applicationHost) throws AuthenticationException {

        // Get account and verify credentials
        Application application = getApplicationForAuthentication(applicationHost);
        User user = getApplicationUserForUsername(username, application);
        if (!AuthenticationUtils.checkPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException(String.format("Password was invalid for user with username %s", username));
        }

        return new AuthenticatedUser(user.getId(), user.getUsername(), application.getId());
    }

    private Application getApplicationForAuthentication(String loginHost) throws AuthenticationException {
        try {
            Application application = applicationDao.getApplicationForHostName(loginHost);
            if (application == null) {
                // TODO we need to handle this better
                throw new AuthenticationException("No application was found to log in to.");
            }

            return application;
        } catch (JpaExecutionException e) {
            throw new AuthenticationException("An unexpected error occurred interfacing with JPA while processing " +
                    "authentication.", e);
        }
    }

    private User getApplicationUserForUsername(String username, Application application) throws AuthenticationException {
        try {
            User user = userDao.getApplicationUserByUsername(application, username);
            if (user == null) {
                throw new UnknownUserException(String.format("No user exists for username %s", username));
            }

            return user;
        } catch (JpaExecutionException e) {
            throw new AuthenticationException("An unexpected error occurred interfacing with JPA while processing " +
                    "authentication.", e);
        }
    }
}
