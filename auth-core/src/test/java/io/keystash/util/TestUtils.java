package io.keystash.util;

import io.keystash.common.models.authentication.oidc.OidcAuthenticationRequest;
import io.keystash.common.models.common.AuthorizationRequest;
import io.keystash.common.models.oauth.OAuth2AuthorizationRequest;

import java.util.Base64;

public final class TestUtils {

    public static final int SAMPLE_USER_ID = 1;
    public static final String SAMPLE_USER_EMAIL = "test@test.com";
    public static final String SAMPLE_USER_PASSWORD = "123";
    public static final String SAMPLE_CLIENT_ID = "my-client";
    public static final String SAMPLE_CLIENT_SECRET = "my-secret";
    public static final String SAMPLE_VALID_REDIRECT_URI = "http://testuri.com/?a=test&b=test";
    public static final String SAMPLE_SECOND_VALID_REDIRECT_URI = "http://testuri2.com/";
    public static final String SAMPLE_STATE = "randomstatestring";
    public static final String SAMPLE_CODE = "0123456789abcdef";
    public static final String SAMPLE_ACCESS_TOKEN = "testaccesstoken";
    public static final String SAMPLE_REFRESH_TOKEN = "testrefreshtoken";
    public static final String SAMPLE_ID_TOKEN = "testidtoken";
    public static final String SAMPLE_SCOPE = "privileged";
    public static final String SAMPLE_AUTHORIZATION_URL = "http://test.com/auth/oauth/authorize";
    public static final String SAMPLE_AUTHENTICATION_URL = "http://test.com/auth/login";

    public static AuthorizationRequest createSampleAuthorizationRequest(String responseType) {
        return createSampleAuthorizationRequest(responseType, false);
    }

    public static AuthorizationRequest createSampleAuthorizationRequest(String responseType, boolean isOpenId) {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = new OAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = new OidcAuthenticationRequest();

        oAuth2AuthorizationRequest.setResponseType(responseType);
        oAuth2AuthorizationRequest.setRedirectUri(SAMPLE_VALID_REDIRECT_URI);
        oAuth2AuthorizationRequest.setClientId(SAMPLE_CLIENT_ID);

        // Setup OpenID Connect
        if (isOpenId) {
            oAuth2AuthorizationRequest.setScope("openid");
        }

        authorizationRequest.setOAuth2AuthorizationRequest(oAuth2AuthorizationRequest);
        authorizationRequest.setOidcAuthenticationRequest(oidcAuthenticationRequest);

        return authorizationRequest;
    }

    public static String createSampleBasicAuthHeader() {
        String authDetails = TestUtils.SAMPLE_CLIENT_ID + ":" + TestUtils.SAMPLE_CLIENT_SECRET;
        return "Basic " + new String(Base64.getEncoder().encode(authDetails.getBytes()));
    }
}
