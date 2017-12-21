package io.keystash.core.resources.authentication;

import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.models.authentication.oidc.UserInfo;
import io.keystash.common.models.error.ErrorType;
import io.keystash.common.util.AuthorizationUtils;
import io.keystash.core.services.authentication.UserInfoService;
import io.keystash.common.services.jose.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A resource for UserInfo requests as defined by OpenID Connect Core
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.5.3">
 *     http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.5.3</a>
 */
@Slf4j
@Path("/userinfo")
public class UserInfoResource {

    private final TokenService tokenService;
    private final UserInfoService userInfoService;

    @Inject
    public UserInfoResource(TokenService tokenService, UserInfoService userInfoService) {
        this.tokenService = tokenService;
        this.userInfoService = userInfoService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfoForUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String accessToken = AuthorizationUtils.getAccessTokenFromHeader(authorizationHeader, tokenService);
        String userIdClaim = tokenService.getTokenClaim(accessToken, "account_id");

        try {
            int userId = Integer.parseInt(userIdClaim);
            UserInfo userInfo = userInfoService.getUserInfo(userId);

            return Response.ok(userInfo).build();
        } catch (NumberFormatException e) {
            log.warn("Token did not contain a valid user_id claim - [{}]", userIdClaim);
            throw new OAuth2Exception(ErrorType.OAuth2.INVALID_ACCESS_TOKEN, Response.Status.UNAUTHORIZED);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfoUsingPost(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        // We must support POST as per spec, but there is no difference in the request so just delegate to the main logic
        return getInfoForUser(authorizationHeader);
    }
}
