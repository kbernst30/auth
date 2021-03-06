package io.keystash.core.services.authentication;

import io.keystash.common.exceptions.OpenIdConnectException;
import io.keystash.common.util.AuthorizationUtils;
import io.keystash.core.exceptions.authentication.UnknownUserInfoException;
import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.authentication.oidc.UserInfo;
import io.keystash.common.models.error.ErrorType;
import io.keystash.common.models.jpa.Account;
import io.keystash.common.persistence.AccountDao;
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
        userInfo.setSub(AuthorizationUtils.getSubjectIdentifierForUser(new AuthenticatedUser(account.getId(), account.getEmail())));
        return userInfo;
    }
}
