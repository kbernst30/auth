package io.keystash.core.services.authentication;

import io.keystash.common.exceptions.OpenIdConnectException;
import io.keystash.common.models.jpa.User;
import io.keystash.common.util.AuthorizationUtils;
import io.keystash.core.exceptions.authentication.UnknownUserInfoException;
import io.keystash.common.exceptions.jpa.JpaExecutionException;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.authentication.oidc.UserInfo;
import io.keystash.common.models.error.ErrorType;
import io.keystash.common.persistence.UserDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DefaultUserInfoService implements UserInfoService {

    private final UserDao userDao;

    @Inject
    public DefaultUserInfoService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserInfo getUserInfo(int userId) {
        User user;
        try {
            user = userDao.getUserById(userId);
        } catch (JpaExecutionException e) {
            log.error("An unknown error occurred fetching account [{}] using JPA", userId, e);
            throw new OpenIdConnectException(ErrorType.OpenIdConnect.SERVER_ERROR);
        }

        if (user == null) {
            throw new UnknownUserInfoException(String.format("No user was found for userId [%d]", userId));
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setPreferredUsername(user.getUsername());
        userInfo.setUserId(user.getId());
        userInfo.setSub(AuthorizationUtils.getSubjectIdentifierForUser(new AuthenticatedUser(user.getId(), user.getUsername())));
        return userInfo;
    }
}
