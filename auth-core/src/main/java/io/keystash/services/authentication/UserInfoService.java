package io.keystash.services.authentication;

import io.keystash.common.models.authentication.oidc.UserInfo;

/**
 * Service to provide UserInfo as specified by OpenId Connect Core
 */
public interface UserInfoService {

    /**
     * Return a UserInfo object associated with a given user id
     * @param userId the ID of the user that info is being fetched for
     * @return a new instance of UserInfo
     */
    UserInfo getUserInfo(int userId);

}
