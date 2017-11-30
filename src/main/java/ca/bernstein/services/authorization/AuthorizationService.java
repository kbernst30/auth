package ca.bernstein.services.authorization;

import ca.bernstein.exceptions.authentication.AuthenticationException;
import ca.bernstein.exceptions.authorization.*;
import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.authentication.oidc.OidcAuthenticationRequest;
import ca.bernstein.models.authentication.oidc.OidcResponseType;
import ca.bernstein.models.common.AuthorizationRequest;
import ca.bernstein.models.common.AuthorizationResponseType;
import ca.bernstein.models.jpa.AllowedScope;
import ca.bernstein.models.jpa.PlatformClient;
import ca.bernstein.models.common.BasicAuthorizationDetails;
import ca.bernstein.models.oauth.*;
import ca.bernstein.persistence.PlatformClientDao;
import ca.bernstein.persistence.ScopeDao;
import ca.bernstein.services.authentication.AuthenticationService;
import ca.bernstein.services.cache.Cache;
import ca.bernstein.services.cache.CacheBuilder;
import ca.bernstein.services.jose.TokenService;
import ca.bernstein.util.AuthenticationUtils;
import ca.bernstein.util.AuthorizationUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@Slf4j
public class AuthorizationService {

    private static final int AUTH_CODE_LENGTH = 20;
    private static final int TOKEN_EXPIRY_TIME_SECONDS = 3600;

    private final AuthenticationService authenticationService;
    private final PlatformClientDao platformClientDao;
    private final ScopeDao scopeDao;
    private final TokenService tokenService;

    private final RandomStringGenerator authCodeGenerator;
    private final Cache<String, OAuth2AuthCode> authCodeCache;


    @Inject
    public AuthorizationService(AuthenticationService authenticationService, PlatformClientDao platformClientDao,
                                ScopeDao scopeDao, TokenService tokenService) {

        this.authenticationService = authenticationService;
        this.platformClientDao = platformClientDao;
        this.scopeDao = scopeDao;
        this.tokenService = tokenService;

        this.authCodeGenerator  = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(LETTERS, DIGITS)
                .build();

        this.authCodeCache = CacheBuilder.createBuilder()
            .withExpiryTime(10, TimeUnit.MINUTES)
            .build();
    }

    @Transactional
    public OAuth2AuthCode generateAuthorizationCode(AuthorizationRequest authorizationRequest, AuthenticatedUser authenticatedUser) {

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = authorizationRequest.getOidcAuthenticationRequest();

        String clientId = oAuth2AuthorizationRequest.getClientId();
        Set<String> requestedScopes = AuthorizationUtils.getScopes(oAuth2AuthorizationRequest.getScope());
        Set<AuthorizationResponseType> responseTypes = oAuth2AuthorizationRequest.getResponseTypes();
        String redirectUri = oAuth2AuthorizationRequest.getRedirectUri();

        PlatformClient client = getPlatformClientFromClientId(clientId);
        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.AUTHORIZATION_CODE)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request an " +
                    "authorization code", clientId), clientId, OAuth2GrantType.AUTHORIZATION_CODE.name().toLowerCase());
        }

        Set<String> resolvedScopes = getResolvedClientScope(client, requestedScopes);

        boolean isTokenResponseRequested = authorizationRequest.isOpenIdConnectAuthRequest() && responseTypes.contains(OAuth2ResponseType.TOKEN);
        boolean isIdTokenResponseRequested = authorizationRequest.isOpenIdConnectAuthRequest() && responseTypes.contains(OidcResponseType.ID_TOKEN);

        // We will add synchronization code here. Although the cache implementation might be thread safe (as is the case
        // for the current in memory implementation using Guava Cache), we cannot guarantee that every implementation will
        // be so. The synchronized block will add additional safety
        OAuth2AuthCode oAuth2AuthCode = getExistingAuthCode(clientId, authenticatedUser, isTokenResponseRequested, isIdTokenResponseRequested);
        if (oAuth2AuthCode == null) {
            synchronized (authCodeCache) {
                oAuth2AuthCode = getExistingAuthCode(clientId, authenticatedUser, isTokenResponseRequested, isIdTokenResponseRequested);
                if (oAuth2AuthCode == null) {
                    String authorizationCodeStr = authCodeGenerator.generate(AUTH_CODE_LENGTH);
                    while (authCodeCache.has(authorizationCodeStr)) {
                        authorizationCodeStr = authCodeGenerator.generate(AUTH_CODE_LENGTH);
                    }

                    oAuth2AuthCode = new OAuth2AuthCode(authorizationCodeStr, clientId, resolvedScopes, redirectUri,
                            authenticatedUser);

                    if (authorizationRequest.isOpenIdConnectAuthRequest()) {
                        oAuth2AuthCode.setNonce(oidcAuthenticationRequest.getNonce());
                    }

                    if (isTokenResponseRequested) {
                        oAuth2AuthCode.setAccessToken(createAccessToken(clientId, authenticatedUser, resolvedScopes));
                    }

                    if (isIdTokenResponseRequested) {
                        oAuth2AuthCode.setIdToken(createIdToken(clientId, authenticatedUser, oAuth2AuthCode.getAccessToken(), oidcAuthenticationRequest.getNonce()));
                    }

                    authCodeCache.set(authorizationCodeStr, oAuth2AuthCode);
                }
            }
        }

        return oAuth2AuthCode;
    }

    @Transactional
    public OAuth2TokenResponse getTokenResponseForImplicitGrant(AuthorizationRequest authorizationRequest, AuthenticatedUser authenticatedUser) {

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = authorizationRequest.getOAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = authorizationRequest.getOidcAuthenticationRequest();

        String clientId = oAuth2AuthorizationRequest.getClientId();
        Set<String> requestedScopes = AuthorizationUtils.getScopes(oAuth2AuthorizationRequest.getScope());
        Set<AuthorizationResponseType> responseTypes = oAuth2AuthorizationRequest.getResponseTypes();

        PlatformClient client = getPlatformClientFromClientId(clientId);
        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.IMPLICIT)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request a " +
                    "token using implicit grant.", clientId), clientId, OAuth2GrantType.IMPLICIT.name().toLowerCase());
        }

        boolean isTokenResponseRequested = responseTypes.contains(OAuth2ResponseType.TOKEN);
        boolean isIdTokenResponseRequested = authorizationRequest.isOpenIdConnectAuthRequest() && responseTypes.contains(OidcResponseType.ID_TOKEN);

        Set<String> resolvedScopes = Collections.emptySet();
        String token = null;
        String idToken = null;

        if (isTokenResponseRequested) {
            resolvedScopes = getResolvedClientScope(client, requestedScopes);
            token = createAccessToken(clientId, authenticatedUser, resolvedScopes);
        }

        if (isIdTokenResponseRequested) {
            idToken = createIdToken(clientId, authenticatedUser, token, oidcAuthenticationRequest.getNonce());
        }

        return createOauth2TokenResponse(token, null, idToken, resolvedScopes);
    }

    @Transactional
    public OAuth2TokenResponse getTokenResponseForAuthorizationCodeGrant(String code, BasicAuthorizationDetails authorizationDetails, String redirectUri) {

        String clientId = authorizationDetails.getClientId();
        PlatformClient client = getPlatformClientFromClientId(clientId);
        verifyClientAuthorizationValidity(authorizationDetails, client);

        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.AUTHORIZATION_CODE)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request a " +
                    "token using authorization_code grant.", clientId), clientId, OAuth2GrantType.AUTHORIZATION_CODE.name().toLowerCase());
        }

        // Get the code and check validity
        OAuth2AuthCode oAuth2AuthCode = authCodeCache.get(code);
        if (oAuth2AuthCode == null
                || !Objects.equals(code, oAuth2AuthCode.getCode())
                || !Objects.equals(client.getClientId(), oAuth2AuthCode.getClientId())
                || !Objects.equals(redirectUri, oAuth2AuthCode.getRedirectUri())) {

            throw new InvalidAuthorizationCodeException(String.format("Provided code [%s] is not a valid authorization " +
                    "code for client [%s].", code, clientId), code, clientId);
        }

        authCodeCache.evict(code); // Evict the code so nobody can use it again

        Set<String> resolvedScopes = oAuth2AuthCode.getResolvedScopes();
        String token = createAccessToken(client.getClientId(), oAuth2AuthCode.getAuthenticatedUser(), oAuth2AuthCode.getResolvedScopes());
        String idToken = null;

        if (resolvedScopes.contains(AuthenticationUtils.OPEN_ID_SCOPE)) {
            if (oAuth2AuthCode.getAccessToken() != null) {
                token = oAuth2AuthCode.getAccessToken();
            }

            if (oAuth2AuthCode.getIdToken() != null) {
                idToken = oAuth2AuthCode.getIdToken();
            } else {
                idToken = createIdToken(clientId, oAuth2AuthCode.getAuthenticatedUser(), token, oAuth2AuthCode.getNonce());
            }
        }

        return createOauth2TokenResponse(token, null, idToken, resolvedScopes);
    }

    @Transactional
    public OAuth2TokenResponse getTokenResponseForClientCredentialsGrant(BasicAuthorizationDetails authorizationDetails, Set<String> requestedScopes) {

        String clientId = authorizationDetails.getClientId();
        PlatformClient client = getPlatformClientFromClientId(authorizationDetails.getClientId());
        verifyClientAuthorizationValidity(authorizationDetails, client);

        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.CLIENT_CREDENTIALS)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request a " +
                    "token using client_credentials grant.", clientId), clientId, OAuth2GrantType.CLIENT_CREDENTIALS.name().toLowerCase());
        }

        Set<String> resolvedScopes = getResolvedClientScope(client, requestedScopes);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(client.getAccount().getId(), client.getAccount().getEmail());

        String token = createAccessToken(client.getClientId(), authenticatedUser, resolvedScopes);
        return createOauth2TokenResponse(token, resolvedScopes);
    }

    @Transactional
    public OAuth2TokenResponse getTokenResponseForPasswordGrant(BasicAuthorizationDetails authorizationDetails, String username,
                                                                String password, Set<String> requestedScopes) {

        String clientId = authorizationDetails.getClientId();
        PlatformClient client = getPlatformClientFromClientId(clientId);
        verifyClientAuthorizationValidity(authorizationDetails, client);

        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.PASSWORD)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request a " +
                    "token using client_credentials grant.", clientId), clientId, OAuth2GrantType.PASSWORD.name().toLowerCase());
        }

        AuthenticatedUser authenticatedUser;
        Set<String> resolvedScopes = getResolvedClientScope(client, requestedScopes);

        try {
            authenticatedUser = authenticationService.authenticateAndGetUser(username, password);
        } catch (AuthenticationException e) {
            throw new InvalidUserException("The provided user credentials were invalid for requested authorization", e);
        }

        String token = createAccessToken(client.getClientId(), authenticatedUser, resolvedScopes);
        String refreshToken = null;
        if (client.getAuthorizedGrantTypes().contains(OAuth2GrantType.REFRESH_TOKEN)) {
            refreshToken = createRefreshToken(token);
        }

        return createOauth2TokenResponse(token, refreshToken, resolvedScopes);
    }

    public OAuth2TokenResponse getTokenResponseForRefreshTokenGrant(BasicAuthorizationDetails authorizationDetails, String refreshToken) {
        String clientId = authorizationDetails.getClientId();
        PlatformClient client = getPlatformClientFromClientId(clientId);
        verifyClientAuthorizationValidity(authorizationDetails, client);

        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.REFRESH_TOKEN)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request a " +
                    "token using refresh_token grant.", clientId), clientId, OAuth2GrantType.REFRESH_TOKEN.name().toLowerCase());
        }

        // Check refresh token validity
        if (!tokenService.isTokenValid(refreshToken)) {
            throw new InvalidRefreshTokenException(String.format("Refresh token is invalid for client [%s]", clientId));
        }

        String username = tokenService.getTokenClaim(refreshToken, "username");
        String accountId = tokenService.getTokenClaim(refreshToken, "account_id");
        String scopeStr = tokenService.getTokenClaim(refreshToken, "scope");

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(Integer.parseInt(accountId), username);
        Set<String> scopes = Stream.of(scopeStr.split(" ")).collect(Collectors.toSet());

        String token = createAccessToken(clientId, authenticatedUser, scopes);
        String newRefreshToken = createRefreshToken(token);

        return createOauth2TokenResponse(token, newRefreshToken, scopes);
    }

    private PlatformClient getPlatformClientFromClientId(String clientId) {

        try {
            PlatformClient client = platformClientDao.getPlatformClientByClientId(clientId);
            if (client == null) {
                throw new UnknownClientException(String.format("No client found for client Id [%s]", clientId), clientId);
            }

            return client;
        } catch (JpaExecutionException e) {
            throw new AuthorizationException("An unexpected error occurred interfacing with JPA while processing " +
                    "authorization.", e);
        }
    }

    private void verifyClientAuthorizationValidity(BasicAuthorizationDetails authorizationDetails, PlatformClient client) {

        if (client.getClientSecret() != null &&
                !AuthenticationUtils.checkPassword(authorizationDetails.getClientSecret(), client.getClientSecret())) {

            throw new InvalidClientException(String.format("Client [%s] is invalid or the provided secret was " +
                    "incorrect.", client.getClientId()));
        }
    }

    private Set<String> getResolvedClientScope(PlatformClient platformClient, Set<String> requestedScopes) {

        if (requestedScopes == null || requestedScopes.size() == 0) {
            return Stream.of(platformClient.getScope().split(",")).collect(Collectors.toSet());
        }

        Set<String> allowedScopes;
        try {
            allowedScopes = scopeDao.getAllowedScopes().stream()
                    .map(AllowedScope::getScope)
                    .collect(Collectors.toSet());
        } catch (JpaExecutionException e) {
            throw new AuthorizationException("An unexpected error occurred interfacing with JPA while processing " +
                    "authorization.", e);
        }

        String clientId = platformClient.getClientId();

        List<String> unknownScopes = Lists.newArrayList(Sets.difference(requestedScopes, allowedScopes));
        if (unknownScopes.size() > 0) {
            throw new InvalidScopeException(String.format("An unknown scope was requested by client [%s]", clientId),
                    clientId, unknownScopes.get(0));
        }

        List<String> unauthorizedScopes = Lists.newArrayList(Sets.difference(allowedScopes, requestedScopes));
        if (!allowedScopes.contains("privileged") && unauthorizedScopes.size() > 0) {
            throw new InvalidScopeException(String.format("Client [%s] requested a scope they are not authorized for",
                    clientId), clientId, unauthorizedScopes.get(0));
        }

        return requestedScopes;
    }

    private OAuth2AuthCode getExistingAuthCode(String clientId, AuthenticatedUser authenticatedUser, boolean shouldHaveToken, boolean shouldHaveIdToken) {
        Stream<OAuth2AuthCode> authCodeStream = authCodeCache.values().stream()
                .filter(code -> Objects.equals(code.getClientId(), clientId) && Objects.equals(code.getAuthenticatedUser(), authenticatedUser));

        if (shouldHaveToken) {
            authCodeStream = authCodeStream.filter(code -> Objects.nonNull(code.getAccessToken()));
        }

        if (shouldHaveIdToken) {
            authCodeStream = authCodeStream.filter(code -> Objects.nonNull(code.getIdToken()));
        }

        return authCodeStream.findFirst().orElse(null);
    }

    private String createAccessToken(String clientId, AuthenticatedUser authenticatedUser, Set<String> scopes) {
        Map<String, String> tokenClaims = new HashMap<>();
        tokenClaims.put("username", authenticatedUser.getUsername());
        tokenClaims.put("account_id", String.valueOf(authenticatedUser.getUserId()));
        tokenClaims.put("scope", String.join(" ", scopes));

        try {
            return tokenService.createAccessToken(tokenClaims, TOKEN_EXPIRY_TIME_SECONDS);
        } catch (TokenException e) {
            throw new AuthorizationException(String.format("Failed to create a new access token for client [%s]", clientId), e);
        }
    }

    private String createRefreshToken(String accessToken) {
        try {
            return tokenService.createRefreshToken(accessToken);
        } catch (TokenException e) {
            throw new AuthorizationException("Failed to create a new refresh token", e);
        }
    }

    private String createIdToken(String clientId, AuthenticatedUser authenticatedUser, String accessToken, String nonce) {
        try {
            return tokenService.createIdToken(clientId, authenticatedUser, accessToken, nonce, TOKEN_EXPIRY_TIME_SECONDS);
        } catch (TokenException e) {
            throw new AuthorizationException("Failed to create a new ID token", e);
        }
    }

    private OAuth2TokenResponse createOauth2TokenResponse(String accessToken, Set<String> scopes) {
        return createOauth2TokenResponse(accessToken, null, scopes);
    }

    private OAuth2TokenResponse createOauth2TokenResponse(String accessToken, String refreshToken, Set<String> scopes) {
        return createOauth2TokenResponse(accessToken, refreshToken, null, scopes);
    }

    private OAuth2TokenResponse createOauth2TokenResponse(String accessToken, String refreshToken, String idToken, Set<String> scopes) {
        OAuth2TokenResponse oAuth2TokenResponse = new OAuth2TokenResponse();
        oAuth2TokenResponse.setAccessToken(accessToken);
        oAuth2TokenResponse.setRefreshToken(refreshToken);
        oAuth2TokenResponse.setIdToken(idToken);
        oAuth2TokenResponse.setExpiryTime(TOKEN_EXPIRY_TIME_SECONDS);
        oAuth2TokenResponse.setTokenType("bearer");
        oAuth2TokenResponse.setScope(StringUtils.isEmpty(accessToken) || scopes.isEmpty() ? null : String.join(" ", scopes));
        return oAuth2TokenResponse;
    }
}
