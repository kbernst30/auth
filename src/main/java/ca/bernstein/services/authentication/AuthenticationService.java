package ca.bernstein.services.authentication;

import ca.bernstein.exceptions.authentication.AuthenticationException;
import ca.bernstein.exceptions.authentication.InvalidCredentialsException;
import ca.bernstein.exceptions.authentication.UnknownAccountException;
import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.authentication.LoginRequest;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.jpa.Account;
import ca.bernstein.persistence.AccountDao;
import ca.bernstein.util.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

@Slf4j
public class AuthenticationService {

    private final AccountDao accountDao;
    private final Provider<HttpSession> httpSessionProvider;

    @Inject
    public AuthenticationService(AccountDao accountDao, Provider<HttpSession> httpSessionProvider) {
        this.accountDao = accountDao;
        this.httpSessionProvider = httpSessionProvider;
    }

    @Transactional
    public void login(LoginRequest loginRequest) throws AuthenticationException {

        // TODO support social logins

        HttpSession httpSession = httpSessionProvider.get();
        if (AuthenticationUtils.isValidSession(httpSession)) {
            httpSession.invalidate();
        }

        // Set new session attributes
        AuthenticatedUser authenticatedUser = authenticateAndGetUser(loginRequest.getUsername(), loginRequest.getPassword());
        httpSession.setAttribute(AuthenticationUtils.AUTHENTICATED_USER, authenticatedUser);
    }

    @Transactional
    public AuthenticatedUser authenticateAndGetUser(String username, String password) throws AuthenticationException {

        // Get account and verify credentials
        Account account = getAccountByEmail(username);
        if (!AuthenticationUtils.checkPassword(password, account.getPassword())) {
            throw new InvalidCredentialsException(String.format("Password was invalid for account with email %s", username));
        }

        return new AuthenticatedUser(account.getId(), account.getEmail());
    }

    private Account getAccountByEmail(String email) throws AuthenticationException {
        try {
            Account account = accountDao.getAccountByEmail(email);
            if (account == null) {
                throw new UnknownAccountException(String.format("No account exists for email %s", email));
            }

            return account;
        } catch (JpaExecutionException e) {
            throw new AuthenticationException("An unexpected error occurred interfacing with JPA while processing " +
                    "authentication.", e);
        }
    }
}
