package io.keystash.core.resources;

import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.common.AuthorizationRequest;
import io.keystash.common.models.common.BasicAuthorizationDetails;
import io.keystash.common.models.oauth.OAuth2GrantType;
import io.keystash.core.resources.authorization.AuthorizationResource;
import io.keystash.core.services.authorization.AuthorizationService;
import io.keystash.core.util.AuthenticationUtils;
import io.keystash.core.util.TestUtils;
import io.keystash.common.models.oauth.OAuth2AuthCode;
import io.keystash.common.models.oauth.OAuth2TokenRequest;
import io.keystash.common.models.oauth.OAuth2TokenResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationResourceTest {

    private static final AuthenticatedUser SAMPLE_AUTHENTICATED_USER = new AuthenticatedUser(TestUtils.SAMPLE_USER_ID,
            TestUtils.SAMPLE_USER_EMAIL, TestUtils.SAMPLE_APPLICATION_ID);

    private static HttpSession mockHttpSession;
    private static AuthorizationService mockAuthorizationService;
    private static UriInfo mockUriInfo;

    private static AuthorizationResource authorizationResource;

    @BeforeClass
    public static void setupTests() {
        mockHttpSession = Mockito.mock(HttpSession.class);
        mockAuthorizationService = Mockito.mock(AuthorizationService.class);
        mockUriInfo = Mockito.mock(UriInfo.class);

        Mockito.when(mockHttpSession.isNew()).thenReturn(false);
        Mockito.when(mockHttpSession.getAttribute(AuthenticationUtils.AUTHENTICATED_USER)).thenReturn(SAMPLE_AUTHENTICATED_USER);

        Mockito.when(mockUriInfo.getAbsolutePath()).thenReturn(URI.create(TestUtils.SAMPLE_AUTHORIZATION_URL));
        Mockito.when(mockUriInfo.getQueryParameters()).thenReturn(TestUtils.SAMPLE_QUERY_PARAMETERS);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(URI.create(TestUtils.SAMPLE_APPLICATION_URL));

        authorizationResource = new AuthorizationResource(mockAuthorizationService);
    }

    @Test
    public void getOAuth2Authorization_oauthCodeResponse_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code", mockUriInfo);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(false, false);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNull(responseUri.getFragment());

        String redirectQuery = redirectUri.getQuery();
        String responseQuery = responseUri.getQuery();

        Assert.assertTrue(responseQuery.contains(redirectQuery));
        Assert.assertTrue(responseQuery.contains("code=" + TestUtils.SAMPLE_CODE));
    }

    @Test
    public void getOAuth2Authorization_oauthCodeResponseWithState_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code", mockUriInfo);
        authorizationRequest.getOAuth2AuthorizationRequest().setState(TestUtils.SAMPLE_STATE);

        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(false, false);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNull(responseUri.getFragment());

        String redirectQuery = redirectUri.getQuery();
        String responseQuery = responseUri.getQuery();

        Assert.assertTrue(responseQuery.contains(redirectQuery));
        Assert.assertTrue(responseQuery.contains("code=" + TestUtils.SAMPLE_CODE));
        Assert.assertTrue(responseQuery.contains("state=" + TestUtils.SAMPLE_STATE));
    }

    @Test
    public void getOAuth2Authorization_implicitAccessToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token", mockUriInfo);
        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, false, false);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.any(AuthorizationRequest.class),
                Mockito.eq(SAMPLE_AUTHENTICATED_USER))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        String responseFragment = responseUri.getFragment();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNotNull(responseFragment);
        Assert.assertTrue(!StringUtils.isEmpty(responseFragment));

        Assert.assertTrue(responseFragment.contains("token_type=bearer"));
        Assert.assertTrue(responseFragment.contains("access_token=" + oAuth2TokenResponse.getAccessToken()));
        Assert.assertTrue(responseFragment.contains("expires_in=" + oAuth2TokenResponse.getExpiryTime()));
        Assert.assertTrue(responseFragment.contains("scope=" + oAuth2TokenResponse.getScope()));
        Assert.assertTrue(!responseFragment.contains("refresh_token="));
        Assert.assertTrue(!responseFragment.contains("id_token="));
    }

    @Test
    public void getOAuth2Authorization_implicitIdToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token", mockUriInfo, true);
        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(false, true, false);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.any(AuthorizationRequest.class),
                Mockito.eq(SAMPLE_AUTHENTICATED_USER))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        String responseFragment = responseUri.getFragment();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNotNull(responseFragment);
        Assert.assertTrue(!StringUtils.isEmpty(responseFragment));

        Assert.assertTrue(responseFragment.contains("token_type=bearer"));
        Assert.assertTrue(responseFragment.contains("id_token=" + oAuth2TokenResponse.getIdToken()));
        Assert.assertTrue(!responseFragment.contains("scope="));
        Assert.assertTrue(!responseFragment.contains("refresh_token="));
        Assert.assertTrue(!responseFragment.contains("access_token="));
        Assert.assertTrue(!responseFragment.contains("expires_in="));
    }

    @Test
    public void getOAuth2Authorization_implicitAccessAndIdToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token token", mockUriInfo, true);
        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, true, false);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.any(AuthorizationRequest.class),
                Mockito.eq(SAMPLE_AUTHENTICATED_USER))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        String responseFragment = responseUri.getFragment();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNotNull(responseFragment);
        Assert.assertTrue(!StringUtils.isEmpty(responseFragment));

        Assert.assertTrue(responseFragment.contains("token_type=bearer"));
        Assert.assertTrue(responseFragment.contains("access_token=" + oAuth2TokenResponse.getAccessToken()));
        Assert.assertTrue(responseFragment.contains("expires_in=" + oAuth2TokenResponse.getExpiryTime()));
        Assert.assertTrue(responseFragment.contains("scope=" + oAuth2TokenResponse.getScope()));
        Assert.assertTrue(responseFragment.contains("id_token=" + oAuth2TokenResponse.getIdToken()));
        Assert.assertTrue(!responseFragment.contains("refresh_token="));
    }

    @Test
    public void getOAuth2Authorization_hybridCodeAndToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code token", mockUriInfo, true);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(true, false);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        String responseFragment = responseUri.getFragment();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNotNull(responseFragment);
        Assert.assertTrue(!StringUtils.isEmpty(responseFragment));

        Assert.assertTrue(responseFragment.contains("code=" + oAuth2AuthCode.getCode()));
        Assert.assertTrue(responseFragment.contains("token_type=bearer"));
        Assert.assertTrue(responseFragment.contains("access_token=" + oAuth2AuthCode.getAccessToken()));
        Assert.assertTrue(responseFragment.contains("scope=" + String.join(" ", oAuth2AuthCode.getResolvedScopes())));
        Assert.assertTrue(!responseFragment.contains("id_token="));
    }

    @Test
    public void getOAuth2Authorization_hybridCodeAndIdToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token", mockUriInfo, true);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(false, true);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        String responseFragment = responseUri.getFragment();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNotNull(responseFragment);
        Assert.assertTrue(!StringUtils.isEmpty(responseFragment));

        Assert.assertTrue(responseFragment.contains("code=" + oAuth2AuthCode.getCode()));
        Assert.assertTrue(responseFragment.contains("id_token=" + oAuth2AuthCode.getIdToken()));
        Assert.assertTrue(!responseFragment.contains("token_type="));
        Assert.assertTrue(!responseFragment.contains("scope="));
        Assert.assertTrue(!responseFragment.contains("access_token="));
    }

    @Test
    public void getOAuth2Authorization_hybridCodeAndAccessTokenAndIdToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token token", mockUriInfo, true);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(true, true);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession);

        Assert.assertEquals(response.getStatus(), Response.Status.TEMPORARY_REDIRECT.getStatusCode());

        URI redirectUri = URI.create(TestUtils.SAMPLE_VALID_REDIRECT_URI);
        URI responseUri = response.getLocation();

        String responseFragment = responseUri.getFragment();

        Assert.assertEquals(redirectUri.getScheme(), responseUri.getScheme());
        Assert.assertEquals(redirectUri.getHost(), responseUri.getHost());
        Assert.assertEquals(redirectUri.getPath(), responseUri.getPath());
        Assert.assertNotNull(responseFragment);
        Assert.assertTrue(!StringUtils.isEmpty(responseFragment));

        Assert.assertTrue(responseFragment.contains("code=" + oAuth2AuthCode.getCode()));
        Assert.assertTrue(responseFragment.contains("id_token=" + oAuth2AuthCode.getIdToken()));
        Assert.assertTrue(responseFragment.contains("token_type=bearer"));
        Assert.assertTrue(responseFragment.contains("access_token=" + oAuth2AuthCode.getAccessToken()));
        Assert.assertTrue(responseFragment.contains("scope=" + String.join(" ", oAuth2AuthCode.getResolvedScopes())));
    }

    @Test
    public void getOAuth2Token_authCodeGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.AUTHORIZATION_CODE, mockUriInfo);
        oAuth2TokenRequest.setCode(TestUtils.SAMPLE_CODE);
        oAuth2TokenRequest.setRedirectUri(TestUtils.SAMPLE_VALID_REDIRECT_URI);

        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, false, false);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.eq(oAuth2TokenRequest),
                Mockito.any(BasicAuthorizationDetails.class))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Token(TestUtils.createSampleBasicAuthHeader(), oAuth2TokenRequest);
        Object responseEntity = response.getEntity();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(responseEntity.getClass(), OAuth2TokenResponse.class);

        OAuth2TokenResponse tokenResponseEntity = (OAuth2TokenResponse) responseEntity;

        Assert.assertEquals(tokenResponseEntity.getAccessToken(), TestUtils.SAMPLE_ACCESS_TOKEN);
        Assert.assertTrue(tokenResponseEntity.getExpiryTime() > 0);
        Assert.assertNull(tokenResponseEntity.getRefreshToken());
        Assert.assertNull(tokenResponseEntity.getIdToken());
        Assert.assertEquals(tokenResponseEntity.getTokenType(), "bearer");
        Assert.assertNotNull(tokenResponseEntity.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(tokenResponseEntity.getScope()));
    }

    @Test
    public void getOAuth2Token_clientCredentialsGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.CLIENT_CREDENTIALS, mockUriInfo);

        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, false, false);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.eq(oAuth2TokenRequest),
                Mockito.any(BasicAuthorizationDetails.class))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Token(TestUtils.createSampleBasicAuthHeader(), oAuth2TokenRequest);
        Object responseEntity = response.getEntity();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(responseEntity.getClass(), OAuth2TokenResponse.class);

        OAuth2TokenResponse tokenResponseEntity = (OAuth2TokenResponse) responseEntity;

        Assert.assertEquals(tokenResponseEntity.getAccessToken(), TestUtils.SAMPLE_ACCESS_TOKEN);
        Assert.assertTrue(tokenResponseEntity.getExpiryTime() > 0);
        Assert.assertNull(tokenResponseEntity.getRefreshToken());
        Assert.assertNull(tokenResponseEntity.getIdToken());
        Assert.assertEquals(tokenResponseEntity.getTokenType(), "bearer");
        Assert.assertNotNull(tokenResponseEntity.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(tokenResponseEntity.getScope()));
    }

    @Test
    public void getOAuth2Token_passwordGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.PASSWORD, mockUriInfo);
        oAuth2TokenRequest.setUsername(TestUtils.SAMPLE_USER_EMAIL);
        oAuth2TokenRequest.setPassword(TestUtils.SAMPLE_USER_PASSWORD);

        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, false, true);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.eq(oAuth2TokenRequest),
                Mockito.any(BasicAuthorizationDetails.class))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Token(TestUtils.createSampleBasicAuthHeader(), oAuth2TokenRequest);
        Object responseEntity = response.getEntity();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(responseEntity.getClass(), OAuth2TokenResponse.class);

        OAuth2TokenResponse tokenResponseEntity = (OAuth2TokenResponse) responseEntity;

        Assert.assertEquals(tokenResponseEntity.getAccessToken(), TestUtils.SAMPLE_ACCESS_TOKEN);
        Assert.assertTrue(tokenResponseEntity.getExpiryTime() > 0);
        Assert.assertNotNull(tokenResponseEntity.getRefreshToken());
        Assert.assertEquals(tokenResponseEntity.getRefreshToken(), TestUtils.SAMPLE_REFRESH_TOKEN);
        Assert.assertNull(tokenResponseEntity.getIdToken());
        Assert.assertEquals(tokenResponseEntity.getTokenType(), "bearer");
        Assert.assertNotNull(tokenResponseEntity.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(tokenResponseEntity.getScope()));
    }

    @Test
    public void getOAuth2Token_refreshTokenGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.REFRESH_TOKEN, mockUriInfo);
        oAuth2TokenRequest.setRefreshToken(TestUtils.SAMPLE_REFRESH_TOKEN);

        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, false, true);
        Mockito.when(mockAuthorizationService.getTokenResponse(Mockito.eq(oAuth2TokenRequest),
                Mockito.any(BasicAuthorizationDetails.class))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Token(TestUtils.createSampleBasicAuthHeader(), oAuth2TokenRequest);
        Object responseEntity = response.getEntity();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(responseEntity.getClass(), OAuth2TokenResponse.class);

        OAuth2TokenResponse tokenResponseEntity = (OAuth2TokenResponse) responseEntity;

        Assert.assertEquals(tokenResponseEntity.getAccessToken(), TestUtils.SAMPLE_ACCESS_TOKEN);
        Assert.assertTrue(tokenResponseEntity.getExpiryTime() > 0);
        Assert.assertNotNull(tokenResponseEntity.getRefreshToken());
        Assert.assertEquals(tokenResponseEntity.getRefreshToken(), TestUtils.SAMPLE_REFRESH_TOKEN);
        Assert.assertNull(tokenResponseEntity.getIdToken());
        Assert.assertEquals(tokenResponseEntity.getTokenType(), "bearer");
        Assert.assertNotNull(tokenResponseEntity.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(tokenResponseEntity.getScope()));
    }

    private OAuth2AuthCode createSampleAuthCode(boolean withAccessToken, boolean withIdToken) {
        OAuth2AuthCode oAuth2AuthCode = new OAuth2AuthCode(TestUtils.SAMPLE_CODE, TestUtils.SAMPLE_CLIENT_ID,
                Stream.of(TestUtils.SAMPLE_SCOPE).collect(Collectors.toSet()), TestUtils.SAMPLE_VALID_REDIRECT_URI,
                SAMPLE_AUTHENTICATED_USER);

        if (withAccessToken) {
            oAuth2AuthCode.setAccessToken(TestUtils.SAMPLE_ACCESS_TOKEN);
        }

        if (withIdToken) {
            oAuth2AuthCode.setIdToken(TestUtils.SAMPLE_ID_TOKEN);
        }

        return oAuth2AuthCode;
    }

    private OAuth2TokenResponse createSampleTokenResponse(boolean withAccessToken, boolean withIdToken, boolean withRefreshToken) {
        OAuth2TokenResponse oAuth2TokenResponse = new OAuth2TokenResponse();
        oAuth2TokenResponse.setScope(TestUtils.SAMPLE_SCOPE);
        oAuth2TokenResponse.setTokenType("bearer");

        if (withAccessToken) {
            oAuth2TokenResponse.setAccessToken(TestUtils.SAMPLE_ACCESS_TOKEN);
            oAuth2TokenResponse.setExpiryTime(3600);
        }

        if (withIdToken) {
            oAuth2TokenResponse.setIdToken(TestUtils.SAMPLE_ID_TOKEN);
        }

        if (withAccessToken && withRefreshToken) {
            oAuth2TokenResponse.setRefreshToken(TestUtils.SAMPLE_REFRESH_TOKEN);
        }

        return oAuth2TokenResponse;
    }
}
