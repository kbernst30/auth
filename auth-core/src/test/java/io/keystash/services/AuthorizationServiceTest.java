package io.keystash.services;

import io.keystash.exceptions.OAuth2Exception;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.common.AuthorizationRequest;
import io.keystash.common.models.common.BasicAuthorizationDetails;
import io.keystash.common.models.jpa.Account;
import io.keystash.common.models.jpa.AllowedScope;
import io.keystash.common.models.jpa.PlatformClient;
import io.keystash.common.models.jpa.RedirectUri;
import io.keystash.common.models.oauth.OAuth2GrantType;
import io.keystash.persistence.PlatformClientDao;
import io.keystash.persistence.ScopeDao;
import io.keystash.services.authentication.AuthenticationService;
import io.keystash.services.authorization.AuthorizationService;
import io.keystash.services.jose.TokenService;
import io.keystash.util.AuthenticationUtils;
import io.keystash.util.TestUtils;
import io.keystash.common.models.oauth.OAuth2AuthCode;
import io.keystash.common.models.oauth.OAuth2TokenResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationServiceTest {

    private static final Account SAMPLE_ACCOUNT = new Account();
    private static final AuthenticatedUser SAMPLE_AUTHENTICATED_USER = new AuthenticatedUser(TestUtils.SAMPLE_USER_ID,
            TestUtils.SAMPLE_USER_EMAIL);
    private static final PlatformClient SAMPLE_PLATFORM_CLIENT = new PlatformClient();
    private static final BasicAuthorizationDetails SAMPlE_BASIC_AUTH_DETAILS = BasicAuthorizationDetails.fromHeaderString(TestUtils.createSampleBasicAuthHeader());

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
        SAMPLE_ACCOUNT.setVerified(true);
        SAMPLE_ACCOUNT.setEmail(TestUtils.SAMPLE_USER_EMAIL);
        SAMPLE_ACCOUNT.setPassword(AuthenticationUtils.getHashedPassword(TestUtils.SAMPLE_USER_PASSWORD));

        SAMPLE_PLATFORM_CLIENT.setClientId(TestUtils.SAMPLE_CLIENT_ID);
        SAMPLE_PLATFORM_CLIENT.setClientSecret(AuthenticationUtils.getHashedPassword(TestUtils.SAMPLE_CLIENT_SECRET));
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
        Mockito.when(mockTokenService.createIdToken(Mockito.anyString(), Mockito.any(AuthenticatedUser.class),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(TestUtils.SAMPLE_ID_TOKEN);
        Mockito.when(mockTokenService.getTokenClaim(Mockito.anyString(), Mockito.eq("account_id")))
                .thenReturn(String.valueOf(TestUtils.SAMPLE_USER_ID));
        Mockito.when(mockTokenService.getTokenClaim(Mockito.anyString(), Mockito.eq("scope"))).thenReturn(TestUtils.SAMPLE_SCOPE);
    }

    @Before
    public void resetForTests() throws Exception {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values()).collect(Collectors.toSet()));
        SAMPLE_PLATFORM_CLIENT.setScope(TestUtils.SAMPLE_SCOPE);
        SAMPLE_PLATFORM_CLIENT.setRedirectUris(getListOfSingleRedirectUri(TestUtils.SAMPLE_VALID_REDIRECT_URI));

        Mockito.when(mockAuthenticationService.authenticateAndGetUser(Mockito.anyString(), Mockito.anyString())).thenReturn(SAMPLE_AUTHENTICATED_USER);

        // We want a new instance before every test to ensure clean cache
        authorizationService = new AuthorizationService(mockAuthenticationService, mockPlatformClientDao, mockScopeDao, mockTokenService);
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

    @Test(expected = OAuth2Exception.class)
    public void generateAuthorizationCode_unknownRedirectUri_doesFail() {
        // Change registered URIs of the client
        SAMPLE_PLATFORM_CLIENT.setRedirectUris(getListOfSingleRedirectUri(TestUtils.SAMPLE_SECOND_VALID_REDIRECT_URI));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code");
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

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForImplicitGrant_implicitGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.IMPLICIT))
                .collect(Collectors.toSet()));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token");
        authorizationService.getTokenResponseForImplicitGrant(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForImplicitGrant_unknownRedirectUri_doesFail() {
        // Change registered URIs of the client
        SAMPLE_PLATFORM_CLIENT.setRedirectUris(getListOfSingleRedirectUri(TestUtils.SAMPLE_SECOND_VALID_REDIRECT_URI));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token");
        authorizationService.getTokenResponseForImplicitGrant(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test
    public void getTokenResponseForImplicitGrant_tokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token");
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForImplicitGrant(authorizationRequest,
                SAMPLE_AUTHENTICATED_USER);

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertNull(oAuth2TokenResponse.getIdToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test
    public void getTokenResponseForImplicitGrant_idTokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token", true);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForImplicitGrant(authorizationRequest,
                SAMPLE_AUTHENTICATED_USER);

        Assert.assertNotNull(oAuth2TokenResponse.getIdToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getIdToken()));
        Assert.assertNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertNull(oAuth2TokenResponse.getScope());
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test
    public void getTokenResponseForImplicitGrant_tokenIdTokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token token", true);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForImplicitGrant(authorizationRequest,
                SAMPLE_AUTHENTICATED_USER);

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNotNull(oAuth2TokenResponse.getIdToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getIdToken()));
        Assert.assertNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForAuthorizationCodeGrant_codeGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.AUTHORIZATION_CODE))
                .collect(Collectors.toSet()));

        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);
        authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2AuthCode.getCode(), SAMPlE_BASIC_AUTH_DETAILS,
                oAuth2AuthCode.getRedirectUri());
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForAuthorizationCodeGrant_invalidCode_doesFail() {
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);

        // Modify code so it's invalid
        authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2AuthCode.getCode() + "test",
                SAMPlE_BASIC_AUTH_DETAILS, oAuth2AuthCode.getRedirectUri());
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForAuthorizationCodeGrant_invalidRedirectUri_doesFail() {
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);

        // Redirect URI must match so use a different URI from the sample
        authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2AuthCode.getCode(), SAMPlE_BASIC_AUTH_DETAILS,
                "http://thisisadifferenturi.com/");
    }

    @Test
    public void getTokenResponseForAuthorizationCodeGrant_isSuccessful() {
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);

        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2AuthCode.getCode(),
                SAMPlE_BASIC_AUTH_DETAILS, oAuth2AuthCode.getRedirectUri());

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertNull(oAuth2TokenResponse.getIdToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test
    public void getTokenResponseForAuthorizationCodeGrant_withIdToken_isSuccessful() {
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", true);

        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForAuthorizationCodeGrant(oAuth2AuthCode.getCode(),
                SAMPlE_BASIC_AUTH_DETAILS, oAuth2AuthCode.getRedirectUri());

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNotNull(oAuth2TokenResponse.getIdToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getIdToken()));
        Assert.assertNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForClientCredentialsGrant_clientCredentialsGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.CLIENT_CREDENTIALS))
                .collect(Collectors.toSet()));

        authorizationService.getTokenResponseForClientCredentialsGrant(SAMPlE_BASIC_AUTH_DETAILS, new HashSet<>());
    }

    @Test
    public void getTokenResponseForClientCredentialsGrant_isSuccessful() {
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForClientCredentialsGrant(SAMPlE_BASIC_AUTH_DETAILS,
                new HashSet<>());

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertNull(oAuth2TokenResponse.getIdToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForPasswordGrant_passwordGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.PASSWORD))
                .collect(Collectors.toSet()));

        authorizationService.getTokenResponseForPasswordGrant(SAMPlE_BASIC_AUTH_DETAILS, TestUtils.SAMPLE_USER_EMAIL,
                TestUtils.SAMPLE_USER_PASSWORD, new HashSet<>());
    }

    @Test
    public void getTokenResponseForPasswordGrant_isSuccessful() {
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForPasswordGrant(SAMPlE_BASIC_AUTH_DETAILS,
                TestUtils.SAMPLE_USER_EMAIL, TestUtils.SAMPLE_USER_PASSWORD, new HashSet<>());

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNotNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getRefreshToken()));
        Assert.assertNull(oAuth2TokenResponse.getIdToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForRefreshToken_refreshTokenGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.REFRESH_TOKEN))
                .collect(Collectors.toSet()));

        authorizationService.getTokenResponseForRefreshTokenGrant(SAMPlE_BASIC_AUTH_DETAILS, TestUtils.SAMPLE_REFRESH_TOKEN);
    }

    @Test
    public void getTokenResponseForRefreshTokenGrant_isSuccessful() {
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponseForRefreshTokenGrant(SAMPlE_BASIC_AUTH_DETAILS,
                TestUtils.SAMPLE_REFRESH_TOKEN);

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNotNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getRefreshToken()));
        Assert.assertNull(oAuth2TokenResponse.getIdToken());
        Assert.assertNotNull(oAuth2TokenResponse.getScope());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getScope()));
        Assert.assertNotNull(oAuth2TokenResponse.getExpiryTime());
        Assert.assertTrue(oAuth2TokenResponse.getExpiryTime() > 0);
        Assert.assertEquals(oAuth2TokenResponse.getTokenType(), "bearer");
    }

    private OAuth2AuthCode getNewOAuth2AuthCode(String responseType, boolean isOpenId) {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest(responseType, isOpenId);
        return authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    private List<RedirectUri> getListOfSingleRedirectUri(String redirectUriStr) {
        return Stream.of(redirectUriStr)
                .map(uri -> {
                    RedirectUri redirectUri = new RedirectUri();
                    redirectUri.setPlatformClient(SAMPLE_PLATFORM_CLIENT);
                    redirectUri.setValue(uri);
                    return redirectUri;
                })
                .collect(Collectors.toList());
    }
}
