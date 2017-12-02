package io.keystash.util;

import io.keystash.exceptions.OAuth2Exception;
import io.keystash.exceptions.OpenIdConnectException;
import io.keystash.models.authentication.oidc.OidcAuthenticationRequest;
import io.keystash.models.common.AuthorizationRequest;
import io.keystash.models.oauth.OAuth2AuthorizationRequest;
import org.junit.Before;
import org.junit.Test;

public class ValidationsTest {

    private static final String SAMPLE_RESPONSE_TYPE = "code";
    private static final String SAMPLE_CLIENT_ID = "my-client";
    private static final String SAMPLE_VALID_REDIRECT_URI = "http://testuri.com/?a=test&b=test";
    private static final String SAMPLE_INVALID_REDIRECT_URI = "[]sadf!?";
    private static final String SAMPLE_NON_ABSOLUTE_REDIRECT_URI = "testuri.com";
    private static final String SAMPLE_REDIRECT_URI_WITH_FRAGMENT = "http://testuri.com/?a=test&b=test#test";

    private AuthorizationRequest authorizationRequest;

    @Before
    public void setup() {
        authorizationRequest = new AuthorizationRequest();
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = new OAuth2AuthorizationRequest();
        OidcAuthenticationRequest oidcAuthenticationRequest = new OidcAuthenticationRequest();

        authorizationRequest.setOAuth2AuthorizationRequest(oAuth2AuthorizationRequest);
        authorizationRequest.setOidcAuthenticationRequest(oidcAuthenticationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_oauthRequest_multipleResponseTypes_doesFail() {
        authorizationRequest.getOAuth2AuthorizationRequest().setResponseType("code token");
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_openIdRequest_tokenResponseType_doesFail() {
        authorizationRequest.getOAuth2AuthorizationRequest().setResponseType("token");
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_noResponseTypes_doesFail() {
        authorizationRequest.getOAuth2AuthorizationRequest().setResponseType("");
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_noRedirectUri_doesFail() {
        setOAuthRequestDefaults();
        authorizationRequest.getOAuth2AuthorizationRequest().setRedirectUri(null);
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_nonAbsoluteRedirectUri_doesFail() {
        setOAuthRequestDefaults();
        authorizationRequest.getOAuth2AuthorizationRequest().setRedirectUri(SAMPLE_NON_ABSOLUTE_REDIRECT_URI);
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_invalidRedirectUri_doesFail() {
        setOAuthRequestDefaults();
        authorizationRequest.getOAuth2AuthorizationRequest().setRedirectUri(SAMPLE_INVALID_REDIRECT_URI);
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_redirectUriWithFragment_doesFail() {
        setOAuthRequestDefaults();
        authorizationRequest.getOAuth2AuthorizationRequest().setRedirectUri(SAMPLE_REDIRECT_URI_WITH_FRAGMENT);
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OAuth2Exception.class)
    public void validateAuthorizationRequest_noClient_doesFail() {
        setOAuthRequestDefaults();
        authorizationRequest.getOAuth2AuthorizationRequest().setClientId(null);
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test(expected = OpenIdConnectException.class)
    public void validateAuthorizationRequest_openIdRequest_noneAndOtherPromptType_doesFail() {
        setOAuthRequestDefaults();
        authorizationRequest.getOAuth2AuthorizationRequest().setScope("openid");
        authorizationRequest.getOidcAuthenticationRequest().setPrompt("none login");
        Validations.validateAuthorizationRequest(authorizationRequest);
    }

    @Test
    public void validateAuthorizationRequest_oauthRequest_isSuccessful() {
        setOAuthRequestDefaults();

        // Test code response type
        authorizationRequest.getOAuth2AuthorizationRequest().setResponseType("code");
        Validations.validateAuthorizationRequest(authorizationRequest);

        // Test token response type
        authorizationRequest.getOAuth2AuthorizationRequest().setResponseType("token");
        Validations.validateAuthorizationRequest(authorizationRequest);

    }

    private void setOAuthRequestDefaults() {
        authorizationRequest.getOAuth2AuthorizationRequest().setResponseType(SAMPLE_RESPONSE_TYPE);
        authorizationRequest.getOAuth2AuthorizationRequest().setClientId(SAMPLE_CLIENT_ID);
        authorizationRequest.getOAuth2AuthorizationRequest().setRedirectUri(SAMPLE_VALID_REDIRECT_URI);
    }
}
