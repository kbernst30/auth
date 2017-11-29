package ca.bernstein.resources;

import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.authentication.oidc.OidcAuthenticationRequest;
import ca.bernstein.models.common.AuthorizationRequest;
import ca.bernstein.models.oauth.OAuth2AuthCode;
import ca.bernstein.models.oauth.OAuth2AuthorizationRequest;
import ca.bernstein.models.oauth.OAuth2TokenResponse;
import ca.bernstein.resources.authorization.AuthorizationResource;
import ca.bernstein.services.authorization.AuthorizationService;
import ca.bernstein.util.AuthenticationUtils;
import ca.bernstein.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationResourceTest {

    private static final MultivaluedMap<String, String> SAMPLE_QUERY_PARAMETERS = new MultivaluedHashMap<>();

    private static final AuthenticatedUser SAMPLE_AUTHENTICATED_USER = new AuthenticatedUser(TestUtils.SAMPLE_USER_ID,
            TestUtils.SAMPLE_USER_EMAIL);

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
        Mockito.when(mockUriInfo.getQueryParameters()).thenReturn(SAMPLE_QUERY_PARAMETERS);

        authorizationResource = new AuthorizationResource(mockAuthorizationService);
    }

    @Test
    public void getOAuth2Authorization_oauthCodeResponse_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code");
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(false, false);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
    public void getOAuth2Authorization_implicitAccessToken_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token");
        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, false, false);
        Mockito.when(mockAuthorizationService.getTokenResponseForImplicitGrant(Mockito.eq(TestUtils.SAMPLE_CLIENT_ID),
                Mockito.anySetOf(String.class), Mockito.eq(SAMPLE_AUTHENTICATED_USER))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token", true);
        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(false, true, false);
        Mockito.when(mockAuthorizationService.getTokenResponseForImplicitGrant(Mockito.eq(TestUtils.SAMPLE_CLIENT_ID),
                Mockito.anySetOf(String.class), Mockito.eq(SAMPLE_AUTHENTICATED_USER))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token token", true);
        OAuth2TokenResponse oAuth2TokenResponse = createSampleTokenResponse(true, true, false);
        Mockito.when(mockAuthorizationService.getTokenResponseForImplicitGrant(Mockito.eq(TestUtils.SAMPLE_CLIENT_ID),
                Mockito.anySetOf(String.class), Mockito.eq(SAMPLE_AUTHENTICATED_USER))).thenReturn(oAuth2TokenResponse);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code token", true);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(true, false);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token", true);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(false, true);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token token", true);
        OAuth2AuthCode oAuth2AuthCode = createSampleAuthCode(true, true);
        Mockito.when(mockAuthorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER))
                .thenReturn(oAuth2AuthCode);

        Response response = authorizationResource.getOAuth2Authorization(authorizationRequest, mockHttpSession, mockUriInfo);

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
