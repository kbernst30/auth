package ca.bernstein.services.authentication;

import ca.bernstein.exceptions.OpenIdConnectException;
import ca.bernstein.exceptions.authentication.UnknownUserInfoException;
import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.authentication.oidc.UserInfo;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.models.jpa.Account;
import ca.bernstein.persistence.AccountDao;
import ca.bernstein.util.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DefaultUserInfoService implements UserInfoService {

    private final AccountDao accountDao;

    @Inject
    public DefaultUserInfoService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public UserInfo getUserInfo(int userId) {
        Account account;
        try {
            account = accountDao.getAccountById(userId);
        } catch (JpaExecutionException e) {
            log.error("An unknown error occurred fetching account [{}] using JPA", userId, e);
            throw new OpenIdConnectException(ErrorType.OpenIdConnect.SERVER_ERROR);
        }

        if (account == null) {
            throw new UnknownUserInfoException(String.format("No user was found for userId [%d]", userId));
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(account.getEmail());
        userInfo.setUserId(account.getId());
        userInfo.setSub(AuthenticationUtils.getSubjectIdentifierForUser(new AuthenticatedUser(account.getId(), account.getEmail())));
        return userInfo;
    }
}
