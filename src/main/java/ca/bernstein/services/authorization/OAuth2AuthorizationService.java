package ca.bernstein.services.authorization;

import ca.bernstein.exceptions.authorization.*;
import ca.bernstein.exceptions.jpa.JpaExecutionException;
import ca.bernstein.models.authentication.AuthenticatedUser;
import ca.bernstein.models.jpa.AllowedScope;
import ca.bernstein.models.jpa.PlatformClient;
import ca.bernstein.models.oauth.OAuth2AuthCode;
import ca.bernstein.models.oauth.OAuth2GrantType;
import ca.bernstein.models.oauth.OAuth2TokenResponse;
import ca.bernstein.persistence.PlatformClientDao;
import ca.bernstein.persistence.ScopeDao;
import ca.bernstein.services.cache.Cache;
import ca.bernstein.services.cache.CacheBuilder;
import ca.bernstein.services.jose.TokenService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
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
public class OAuth2AuthorizationService {

    private static final int AUTH_CODE_LENGTH = 20;
    private static final int TOKEN_EXPIRY_TIME_SECONDS = 3600;

    private final PlatformClientDao platformClientDao;
    private final ScopeDao scopeDao;
    private final TokenService tokenService;

    private final RandomStringGenerator authCodeGenerator;
    private final Cache<String, OAuth2AuthCode> authCodeCache;


    @Inject
    public OAuth2AuthorizationService(PlatformClientDao platformClientDao, ScopeDao scopeDao, TokenService tokenService) {
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
    public String generateAuthorizationCode(String clientId, Set<String> requestedScopes, AuthenticatedUser authenticatedUser) throws AuthorizationException {

        PlatformClient client = getPlatformClientFromClientId(clientId);
        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.AUTHORIZATION_CODE)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request an " +
                    "authorization code", clientId));
        }

        Set<String> resolvedScopes = getResolvedClientScope(client, requestedScopes);

        // We will add synchronization code here. Although the cache implementation might be thread safe (as is the case
        // for the current in memory implementation using Guava Cache), we cannot guarantee that every implementation will
        // be so. The synchronized block will add additional safety
        OAuth2AuthCode oAuth2AuthCode = getAuthCodeForClientAndUser(clientId, authenticatedUser);
        if (oAuth2AuthCode == null) {
            synchronized (authCodeCache) {
                oAuth2AuthCode = getAuthCodeForClientAndUser(clientId, authenticatedUser);
                if (oAuth2AuthCode == null) {
                    String authorizationCodeStr = authCodeGenerator.generate(AUTH_CODE_LENGTH);
                    while (authCodeCache.has(authorizationCodeStr)) {
                        authorizationCodeStr = authCodeGenerator.generate(AUTH_CODE_LENGTH);
                    }

                    oAuth2AuthCode = new OAuth2AuthCode(authorizationCodeStr, clientId, resolvedScopes, authenticatedUser);
                    authCodeCache.set(authorizationCodeStr, oAuth2AuthCode);
                }
            }
        }

        return oAuth2AuthCode.getCode();
    }

    @Transactional
    public OAuth2TokenResponse getTokenResponseForImplicitGrant(int accountId, String email, String clientId,
                                                                Set<String> requestedScopes) throws AuthorizationException {

        PlatformClient client = getPlatformClientFromClientId(clientId);
        if (!client.getAuthorizedGrantTypes().contains(OAuth2GrantType.IMPLICIT)) {
            throw new UnauthorizedClientException(String.format("Client [%s] is not authorized to request a " +
                    "token using implicit grant.", clientId));
        }

        Set<String> resolvedScopes = getResolvedClientScope(client, requestedScopes);

        Map<String, String> tokenClaims = new HashMap<>();
        tokenClaims.put("email", email);
        tokenClaims.put("account_id", String.valueOf(accountId));

        String token;
        try {
            token = tokenService.createAccessToken(tokenClaims, TOKEN_EXPIRY_TIME_SECONDS);
        } catch (TokenException e) {
            throw new AuthorizationException(String.format("Failed to create a new access token for client [%s]", clientId), e);
        }

        return createOauth2TokenResponse(token, resolvedScopes);
    }

    private PlatformClient getPlatformClientFromClientId(String clientId) throws AuthorizationException {

        try {
            PlatformClient client = platformClientDao.getPlatformClientByClientId(clientId);
            if (client == null) {
                throw new UnknownClientException(String.format("No client found for client Id [%s]", clientId));
            }

            return client;
        } catch (JpaExecutionException e) {
            throw new AuthorizationException("An unexpected error occurred interfacing with JPA while processing " +
                    "authorization.", e);
        }
    }

    private Set<String> getResolvedClientScope(PlatformClient platformClient, Set<String> requestedScopes) throws AuthorizationException {

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

        List<String> unknownScopes = Lists.newArrayList(Sets.difference(requestedScopes, allowedScopes));
        if (unknownScopes.size() > 0) {
            throw new InvalidScopeException(String.format("An unknown scope was requested by client [%s]",
                    platformClient.getClientId()), unknownScopes.get(0));
        }

        List<String> unauthorizedScopes = Lists.newArrayList(Sets.difference(allowedScopes, requestedScopes));
        if (unauthorizedScopes.size() > 0) {
            throw new InvalidScopeException(String.format("Client [%s] requested a scope they are not authorized for",
                    platformClient.getClientId()), unauthorizedScopes.get(0));
        }

        return requestedScopes;
    }

    private OAuth2AuthCode getAuthCodeForClientAndUser(String clientId, AuthenticatedUser authenticatedUser) {
        return authCodeCache.values().stream()
                .filter(code -> Objects.equals(code.getClientId(), clientId) && Objects.equals(code.getAuthenticatedUser(), authenticatedUser))
                .findFirst()
                .orElse(null);
    }

    private OAuth2TokenResponse createOauth2TokenResponse(String accessToken, Set<String> scopes) {
        return createOauth2TokenResponse(accessToken, null, scopes);
    }

    private OAuth2TokenResponse createOauth2TokenResponse(String accessToken, String refreshToken, Set<String> scopes) {
        OAuth2TokenResponse oAuth2TokenResponse = new OAuth2TokenResponse();
        oAuth2TokenResponse.setAccessToken(accessToken);
        oAuth2TokenResponse.setRefreshToken(refreshToken);
        oAuth2TokenResponse.setExpiryTime(TOKEN_EXPIRY_TIME_SECONDS);
        oAuth2TokenResponse.setTokenType("bearer");
        oAuth2TokenResponse.setScope(scopes.isEmpty() ? null : String.join(" ", scopes));
        return oAuth2TokenResponse;
    }
}
