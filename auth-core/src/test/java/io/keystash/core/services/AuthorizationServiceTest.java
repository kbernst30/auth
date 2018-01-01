package io.keystash.core.services;

import io.keystash.common.exceptions.OAuth2Exception;
import io.keystash.common.models.authentication.AuthenticatedUser;
import io.keystash.common.models.common.AuthorizationRequest;
import io.keystash.common.models.common.BasicAuthorizationDetails;
import io.keystash.common.models.jpa.*;
import io.keystash.common.models.oauth.OAuth2GrantType;
import io.keystash.common.models.oauth.OAuth2TokenRequest;
import io.keystash.common.persistence.ApplicationDao;
import io.keystash.common.persistence.ClientDao;
import io.keystash.common.persistence.ScopeDao;
import io.keystash.core.services.authentication.AuthenticationService;
import io.keystash.core.services.authorization.AuthorizationService;
import io.keystash.common.services.jose.TokenService;
import io.keystash.core.util.AuthenticationUtils;
import io.keystash.core.util.TestUtils;
import io.keystash.common.models.oauth.OAuth2AuthCode;
import io.keystash.common.models.oauth.OAuth2TokenResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationServiceTest {

    private static final Account SAMPLE_ACCOUNT = new Account();
    private static final User SAMPLE_USER = new User();
    private static final Application SAMPLE_APPLICATION = new Application();

    private static final AuthenticatedUser SAMPLE_AUTHENTICATED_USER = new AuthenticatedUser(TestUtils.SAMPLE_USER_ID,
            TestUtils.SAMPLE_USER_EMAIL, TestUtils.SAMPLE_APPLICATION_ID);
    private static final Client SAMPLE_PLATFORM_CLIENT = new Client();
    private static final BasicAuthorizationDetails SAMPlE_BASIC_AUTH_DETAILS = BasicAuthorizationDetails.fromHeaderString(TestUtils.createSampleBasicAuthHeader());

    private static AuthenticationService mockAuthenticationService;
    private static ApplicationDao mockApplicationDao;
    private static ClientDao mockClientDao;
    private static ScopeDao mockScopeDao;
    private static TokenService mockTokenService;
    private static UriInfo mockUriInfo;

    private static AuthorizationService authorizationService;

    @BeforeClass
    public static void setupTests() throws Exception {
        mockAuthenticationService = Mockito.mock(AuthenticationService.class);
        mockApplicationDao = Mockito.mock(ApplicationDao.class);
        mockClientDao = Mockito.mock(ClientDao.class);
        mockScopeDao = Mockito.mock(ScopeDao.class);
        mockTokenService = Mockito.mock(TokenService.class);
        mockUriInfo = Mockito.mock(UriInfo.class);

        SAMPLE_ACCOUNT.setId(TestUtils.SAMPLE_ACCOUNT_ID);

        SAMPLE_USER.setVerified(true);
        SAMPLE_USER.setUsername(TestUtils.SAMPLE_USER_EMAIL);
        SAMPLE_USER.setPassword(AuthenticationUtils.getHashedPassword(TestUtils.SAMPLE_USER_PASSWORD));

        SAMPLE_APPLICATION.setId(TestUtils.SAMPLE_APPLICATION_ID);

        SAMPLE_PLATFORM_CLIENT.setClientId(TestUtils.SAMPLE_CLIENT_ID);
        SAMPLE_PLATFORM_CLIENT.setClientSecret(TestUtils.SAMPLE_CLIENT_SECRET);
        SAMPLE_PLATFORM_CLIENT.setAutoApprove(true);
        SAMPLE_PLATFORM_CLIENT.setApplication(SAMPLE_APPLICATION);

        Mockito.when(mockUriInfo.getAbsolutePath()).thenReturn(URI.create(TestUtils.SAMPLE_AUTHORIZATION_URL));
        Mockito.when(mockUriInfo.getQueryParameters()).thenReturn(TestUtils.SAMPLE_QUERY_PARAMETERS);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(URI.create(TestUtils.SAMPLE_APPLICATION_URL));

        Mockito.when(mockApplicationDao.getApplicationForHostName(Mockito.anyString())).thenReturn(SAMPLE_APPLICATION);
        Mockito.when(mockClientDao.getApplicationClientByClientId(SAMPLE_APPLICATION, TestUtils.SAMPLE_CLIENT_ID))
                .thenReturn(SAMPLE_PLATFORM_CLIENT);
        Mockito.when(mockScopeDao.getAllowedScopes()).thenReturn(
                Stream.of("openid").map(scope -> {
                    ApplicationScope applicationScope = new ApplicationScope();
                    applicationScope.setScope(scope);
                    return applicationScope;
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

        Mockito.when(mockAuthenticationService.authenticateAndGetUser(Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(TestUtils.SAMPLE_APPLICATION_URL))).thenReturn(SAMPLE_AUTHENTICATED_USER);

        // We want a new instance before every test to ensure clean cache
        authorizationService = new AuthorizationService(mockAuthenticationService, mockApplicationDao, mockClientDao,
                mockScopeDao, mockTokenService);
    }

    @Test(expected = OAuth2Exception.class)
    public void generateAuthorizationCode_codeGrantNotAllowed_doesFail() {
        SAMPLE_PLATFORM_CLIENT.setAuthorizedGrantTypes(Stream.of(OAuth2GrantType.values())
                .filter(oAuth2GrantType -> !oAuth2GrantType.equals(OAuth2GrantType.AUTHORIZATION_CODE))
                .collect(Collectors.toSet()));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code", mockUriInfo);
        authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test(expected = OAuth2Exception.class)
    public void generateAuthorizationCode_invalidScope_doesFail() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code", mockUriInfo);
        authorizationRequest.getOAuth2AuthorizationRequest().setScope("bad_scope");
        authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test(expected = OAuth2Exception.class)
    public void generateAuthorizationCode_unknownRedirectUri_doesFail() {
        // Change registered URIs of the client
        SAMPLE_PLATFORM_CLIENT.setRedirectUris(getListOfSingleRedirectUri(TestUtils.SAMPLE_SECOND_VALID_REDIRECT_URI));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code", mockUriInfo);
        authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test
    public void generateAuthorizationCode_codeResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code", mockUriInfo);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code token", mockUriInfo, true);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token", mockUriInfo, true);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("code id_token token", mockUriInfo, true);

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

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token", mockUriInfo);
        authorizationService.getTokenResponse(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForImplicitGrant_unknownRedirectUri_doesFail() {
        // Change registered URIs of the client
        SAMPLE_PLATFORM_CLIENT.setRedirectUris(getListOfSingleRedirectUri(TestUtils.SAMPLE_SECOND_VALID_REDIRECT_URI));

        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token", mockUriInfo);
        authorizationService.getTokenResponse(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    @Test
    public void getTokenResponseForImplicitGrant_tokenResponseType_isSuccessful() {
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("token", mockUriInfo);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(authorizationRequest,
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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token", mockUriInfo, true);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(authorizationRequest,
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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest("id_token token", mockUriInfo, true);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(authorizationRequest,
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

        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.AUTHORIZATION_CODE, mockUriInfo);
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);
        oAuth2TokenRequest.setCode(oAuth2AuthCode.getCode());
        oAuth2TokenRequest.setRedirectUri(oAuth2AuthCode.getRedirectUri());
        authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForAuthorizationCodeGrant_invalidCode_doesFail() {
        // Modify code so it's invalid
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.AUTHORIZATION_CODE, mockUriInfo);
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);
        oAuth2TokenRequest.setCode(oAuth2AuthCode.getCode() + "test");
        oAuth2TokenRequest.setRedirectUri(oAuth2AuthCode.getRedirectUri());
        authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);
    }

    @Test(expected = OAuth2Exception.class)
    public void getTokenResponseForAuthorizationCodeGrant_invalidRedirectUri_doesFail() {
        // Redirect URI must match so use a different URI from the sample
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.AUTHORIZATION_CODE, mockUriInfo);
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);
        oAuth2TokenRequest.setCode(oAuth2AuthCode.getCode());
        oAuth2TokenRequest.setRedirectUri("http://thisisadifferenturi.com/");
        authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);
    }

    @Test
    public void getTokenResponseForAuthorizationCodeGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.AUTHORIZATION_CODE, mockUriInfo);
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code", false);
        oAuth2TokenRequest.setCode(oAuth2AuthCode.getCode());
        oAuth2TokenRequest.setRedirectUri(oAuth2AuthCode.getRedirectUri());
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);

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

    @Test
    public void getTokenResponseForAuthorizationCodeGrant_withIdToken_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.AUTHORIZATION_CODE, mockUriInfo);
        OAuth2AuthCode oAuth2AuthCode = getNewOAuth2AuthCode("code,id_token", true);
        oAuth2TokenRequest.setCode(oAuth2AuthCode.getCode());
        oAuth2TokenRequest.setRedirectUri(oAuth2AuthCode.getRedirectUri());
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);

        Assert.assertNotNull(oAuth2TokenResponse.getAccessToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getAccessToken()));
        Assert.assertNotNull(oAuth2TokenResponse.getIdToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getIdToken()));
        Assert.assertNotNull(oAuth2TokenResponse.getRefreshToken());
        Assert.assertTrue(!StringUtils.isEmpty(oAuth2TokenResponse.getRefreshToken()));
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

        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.CLIENT_CREDENTIALS, mockUriInfo);
        authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);
    }

    @Test
    public void getTokenResponseForClientCredentialsGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.CLIENT_CREDENTIALS, mockUriInfo);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);

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

        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.PASSWORD, mockUriInfo);
        oAuth2TokenRequest.setUsername(TestUtils.SAMPLE_USER_EMAIL);
        oAuth2TokenRequest.setPassword(TestUtils.SAMPLE_USER_PASSWORD);
        oAuth2TokenRequest.setScope(TestUtils.SAMPLE_SCOPE);
        authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);
    }

    @Test
    public void getTokenResponseForPasswordGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.PASSWORD, mockUriInfo);
        oAuth2TokenRequest.setUsername(TestUtils.SAMPLE_USER_EMAIL);
        oAuth2TokenRequest.setPassword(TestUtils.SAMPLE_USER_PASSWORD);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);

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

        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.REFRESH_TOKEN, mockUriInfo);
        oAuth2TokenRequest.setRefreshToken(TestUtils.SAMPLE_REFRESH_TOKEN);
        authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);
    }

    @Test
    public void getTokenResponseForRefreshTokenGrant_isSuccessful() {
        OAuth2TokenRequest oAuth2TokenRequest = TestUtils.createSampleTokenRequest(OAuth2GrantType.REFRESH_TOKEN, mockUriInfo);
        oAuth2TokenRequest.setRefreshToken(TestUtils.SAMPLE_REFRESH_TOKEN);
        OAuth2TokenResponse oAuth2TokenResponse = authorizationService.getTokenResponse(oAuth2TokenRequest, SAMPlE_BASIC_AUTH_DETAILS);

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
        AuthorizationRequest authorizationRequest = TestUtils.createSampleAuthorizationRequest(responseType, mockUriInfo, isOpenId);
        return authorizationService.generateAuthorizationCode(authorizationRequest, SAMPLE_AUTHENTICATED_USER);
    }

    private List<RedirectUri> getListOfSingleRedirectUri(String redirectUriStr) {
        return Stream.of(redirectUriStr)
                .map(uri -> {
                    RedirectUri redirectUri = new RedirectUri();
                    redirectUri.setClient(SAMPLE_PLATFORM_CLIENT);
                    redirectUri.setValue(uri);
                    return redirectUri;
                })
                .collect(Collectors.toList());
    }
}
