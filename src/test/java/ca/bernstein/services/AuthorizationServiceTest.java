package ca.bernstein.services;

import ca.bernstein.exceptions.OAuth2Exception;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.authentication.oidc.OidcAuthenticationRequest;
import ca.bernstein.models.common.AuthorizationRequest;
import ca.bernstein.models.jpa.Account;
import ca.bernstein.models.jpa.AllowedScope;
import ca.bernstein.models.jpa.PlatformClient;
import ca.bernstein.models.oauth.OAuth2AuthCode;
import ca.bernstein.models.oauth.OAuth2AuthorizationRequest;
import ca.bernstein.models.oauth.OAuth2GrantType;
import ca.bernstein.persistence.PlatformClientDao;
import ca.bernstein.persistence.ScopeDao;
import ca.bernstein.services.authentication.AuthenticationService;
import ca.bernstein.services.authorization.AuthorizationService;
import ca.bernstein.services.jose.TokenService;
import ca.bernstein.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationServiceTest {

    private static final Account SAMPLE_ACCOUNT = new Account();
    private static final AuthenticatedUser SAMPLE_AUTHENTICATED_USER = new AuthenticatedUser(TestUtils.SAMPLE_USER_ID,
            TestUtils.SAMPLE_USER_EMAIL);
    private static final PlatformClient SAMPLE_PLATFORM_CLIENT = new PlatformClient();

    private static AuthenticationService mockAuthenticationService;
    private static PlatformClientDao mockPlatformClientDao;
    private static ScopeDao mockScopeDao;
    private static TokenService mockTokenService;

    private static AuthorizationService authorizationService;

    @BeforeClass
    public static void setupTests() throws Exception {
        mockAuthenticationService = Mockito.mock(AuthenticationService.class);
        mockPlatformClientDao = Mockito.mock(PlatformClientDao.class);
        mockScopeDao = Mockito.mock(ScopeDao.class);
        mockTokenService = Mockito.mock(TokenService.class);

        SAMPLE_ACCOUNT.setId(TestUtils.SAMPLE_USER_ID);
        SAMPLE_ACCOUNT.setPassword(TestUtils.SAMPLE_USER_PASSWORD);
        SAMPLE_ACCOUNT.setVerified(true);
        SAMPLE_ACCOUNT.setEmail(TestUtils.SAMPLE_USER_EMAIL);

        SAMPLE_PLATFORM_CLIENT.setClientId(TestUtils.SAMPLE_CLIENT_ID);
        SAMPLE_PLATFORM_CLIENT.setClientSecret(TestUtils.SAMPLE_CLIENT_SECRET);
        SAMPLE_PLATFORM_CLIENT.setAutoApprove(true);
        SAMPLE_PLATFORM_CLIENT.setAccount(SAMPLE_ACCOUNT);

        Mockito.when(mockPlatformClientDao.getPlatformClientByClientId(TestUtils.SAMPLE_CLIENT_ID)).thenReturn(SAMPLE_PLATFORM_CLIENT);
        Mockito.when(mockScopeDao.getAllowedScopes()).thenReturn(
                Stream.of("openid").map(scope -> {
                    AllowedScope allowedScope = new AllowedScope();
                    allowedScope.setScope(scope);
                    return allowedScope;
                }).collect(Collectors.toList())
        );

        Mockito.when(mockTokenService.createRefreshToken(Mockito.anyString())).thenReturn(TestUtils.SAMPLE_REFRESH_TOKEN);
        Mockito.when(mockTokenService.isTokenValid(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockTokenService.createAccessToken(Mockito.anyMapOf(String.class, String.class), Mockito.anyInt()))
                .thenReturn(TestUtils.SAMPLE_ACCESS_TOKEN);

        authorizationService = new AuthorizationService(mockAuthenticationService, mockPlatformClientDao, mockScopeDao, mockTokenService);
    }

    @Before
    public void resetForTests() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values()).collect(Collectors.toSet()));
        SAMPLE_PLATFORM_CLIENT.setScope(TestUtils.SAMPLE_SCOPE);
    }

    @Test(expected = OAuth2Exception.class)
    public void generateAuthorizationCode_codeGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.AUTHORIZATION_CODE))
                .collect(Collectors.toSet()));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code");
        authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test(expected = OAuth2Exception.class)
    public void generateAuthorizationCode_invalidScope_doesFail() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code");
        authorizationRequest.getOAuth2AuthorizationRequest().setScope("bad_scope");
        authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test
    public void generateAuthorizationCode_codeResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code");

        OAuth2AuthCode oAuth2AuthCode = authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);

        Assert.assertNull(oAuth2AuthCode.getAccessToken());
        Assert.assertNull(oAuth2AuthCode.getIdToken());
        Assert.assertEquals(oAuth2AuthCode.getClientId(), TestUtils.SAMPLE_CLIENT_ID);
        Assert.assertEquals(oAuth2AuthCode.getAuthenticatedUser(), SAMPLE_AUTHENTICATED_USER);
        Assert.assertEquals(oAuth2AuthCode.getRedirectUri(), TestUtils.SAMPLE_VALID_REDIRECT_URI);
        Assert.assertNotNull(oAuth2AuthCode.getResolvedScopes());
        Assert.assertTrue(!oAuth2AuthCode.getResolvedScopes().isEmpty());
        Assert.assertNotNull(oAuth2AuthCode.getCode());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getCode()));
    }

    @Test
    public void generateAuthorizationCode_codeTokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code token", true);

        OAuth2AuthCode oAuth2AuthCode = authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);

        Assert.assertNotNull(oAuth2AuthCode.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getAccessToken()));
        Assert.assertNull(oAuth2AuthCode.getIdToken());
        Assert.assertEquals(oAuth2AuthCode.getClientId(), TestUtils.SAMPLE_CLIENT_ID);
        Assert.assertEquals(oAuth2AuthCode.getAuthenticatedUser(), SAMPLE_AUTHENTICATED_USER);
        Assert.assertEquals(oAuth2AuthCode.getRedirectUri(), TestUtils.SAMPLE_VALID_REDIRECT_URI);
        Assert.assertNotNull(oAuth2AuthCode.getResolvedScopes());
        Assert.assertTrue(!oAuth2AuthCode.getResolvedScopes().isEmpty());
        Assert.assertNotNull(oAuth2AuthCode.getCode());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getCode()));
    }

    @Test
    public void generateAuthorizationCode_codeIdTokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token", true);

        OAuth2AuthCode oAuth2AuthCode = authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);

        Assert.assertNull(oAuth2AuthCode.getAccessToken());
        Assert.assertNotNull(oAuth2AuthCode.getIdToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getIdToken()));
        Assert.assertEquals(oAuth2AuthCode.getClientId(), TestUtils.SAMPLE_CLIENT_ID);
        Assert.assertEquals(oAuth2AuthCode.getAuthenticatedUser(), SAMPLE_AUTHENTICATED_USER);
        Assert.assertEquals(oAuth2AuthCode.getRedirectUri(), TestUtils.SAMPLE_VALID_REDIRECT_URI);
        Assert.assertNotNull(oAuth2AuthCode.getResolvedScopes());
        Assert.assertTrue(!oAuth2AuthCode.getResolvedScopes().isEmpty());
        Assert.assertNotNull(oAuth2AuthCode.getCode());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getCode()));
    }

    @Test
    public void generateAuthorizationCode_codeTokenIdTokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token token", true);

        OAuth2AuthCode oAuth2AuthCode = authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);

        Assert.assertNotNull(oAuth2AuthCode.getAccessToken());
        Assert.assertNotNull(oAuth2AuthCode.getIdToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getAccessToken()));
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getIdToken()));
        Assert.assertEquals(oAuth2AuthCode.getClientId(), TestUtils.SAMPLE_CLIENT_ID);
        Assert.assertEquals(oAuth2AuthCode.getAuthenticatedUser(), SAMPLE_AUTHENTICATED_USER);
        Assert.assertEquals(oAuth2AuthCode.getRedirectUri(), TestUtils.SAMPLE_VALID_REDIRECT_URI);
        Assert.assertNotNull(oAuth2AuthCode.getResolvedScopes());
        Assert.assertTrue(!oAuth2AuthCode.getResolvedScopes().isEmpty());
        Assert.assertNotNull(oAuth2AuthCode.getCode());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2AuthCode.getCode()));
    }
}
