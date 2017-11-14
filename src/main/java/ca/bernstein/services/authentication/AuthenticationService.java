package ca.bernstein.services.authentication;

import ca.bernstein.exceptions.authentication.AuthenticationException;
import ca.bernstein.exceptions.authentication.InvalidCredentialsException;
import ca.bernstein.exceptions.authentication.UnknownAccountException;
import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.authentication.LoginRequest;
import ca.bernstein.models.authentication.User;
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

        // Get account and verify credentials
        Account account = getAccountByEmail(loginRequest.getUsername());
        if (!AuthenticationUtils.checkPassword(loginRequest.getPassword(), account.getPassword())) {
            throw new InvalidCredentialsException(String.format("Password was invalid for account with email %s",
                    loginRequest.getUsername()));
        }

        // Set new session attributes
        User user = new User(account.getId(), account.getEmail());
        httpSession.setAttribute(AuthenticationUtils.AUTHENTICATED_USER, user);
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
