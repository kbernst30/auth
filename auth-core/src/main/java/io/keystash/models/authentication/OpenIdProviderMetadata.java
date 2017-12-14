package io.keystash.models.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * A configuration object representing OpenID Providers metadata
 * <p>
 *     @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html#rfc.section.3">
 *         http://openid.net/specs/openid-connect-discovery-1_0.html#rfc.section.3
 *         </a>
 * </p>
 */
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenIdProviderMetadata {

    /**
     * The issuer of the tokens
     * <p>THis is the same URL as the iss claim in ID tokens - it must have an https scheme and no query or fragment</p>
     */
    @JsonProperty("issuer")
    @Getter @Setter private String issuer;

    /**
     * The URL of the OAuth2.0 authorization endpoint (i.e. https://auth.com/oauth/authorize)
     */
    @JsonProperty("authorization_endpoint")
    @Getter @Setter private String authorizationEndpoint;

    /**
     * The URL of the OAuth2.0 token endpoint (i.e. https://auth.com/oauth/token)
     */
    @JsonProperty("token_endpoint")
    @Getter @Setter private String tokenEndpoint;

    /**
     * The URL of the OpenID Connect UserInfo endpoint
     */
    @JsonProperty("userinfo_endpoint")
    @Getter @Setter private String userInfoEndpoint;

    /**
     * The URL of the JSON Web Key Set
     * <p>
     *     This contains the signing keys that the client can use to validate signatures from the auth server
     * </p>
     */
    @JsonProperty("jwks_uri")
    @Getter @Setter private String jwksUri;

    /**
     * The URL of the dynamic client registration endpoint
     */
    @JsonProperty("registration_endpoint")
    @Getter @Setter private String registrationEndpoint;

    /**
     * An array containing a list of valid supported scopes
     */
    @JsonProperty("scopes_supported")
    @Getter @Setter private Set<String> scopesSupported;

    /**
     * An array containing a list of valid OAuth2.0 response types
     */
    @JsonProperty("response_types_supported")
    @Getter @Setter private Set<String> responseTypesSupported;

    /**
     * An array containing a list of valid OAuth2.0 response modes
     */
    @JsonProperty("response_modes_supported")
    @Getter @Setter private Set<String> responseModesSupported;

    /**
     * An array containing a list of valid OAuth2.0 grant types
     */
    @JsonProperty("grant_types_supported")
    @Getter @Setter private Set<String> grantTypesSupported;

    /**
     * An array containing a list of supported Authentication Context Class References
     */
    @JsonProperty("acr_values_supported")
    @Getter @Setter private Set<String> acrValuesSupported;

    /**
     * An array containing a list of supported Subject Identifier Types (i.e. pairwise and public)
     */
    @JsonProperty("subject_types_supported")
    @Getter @Setter private Set<String> subjectTypesSupported;

    /**
     * An array containing a list of supported JWS signing algorithms for ID tokens
     * <p>RS256 must be supported</p>
     */
    @JsonProperty("id_token_signing_alg_values_supported")
    @Getter @Setter private Set<String> idTokenSigningAlgValuesSupported;

    /**
     * An array containing a list of supported JWE encryption algorithms (alg value) for ID tokens
     */
    @JsonProperty("id_token_encryption_alg_values_supported")
    @Getter @Setter private Set<String> idTokenEncryptionAlgValuesSupported;

    /**
     * An array containing a list of supported JWE encryption algorithms (enc values) for ID tokens
     */
    @JsonProperty("id_token_encryption_enc_values_supported")
    @Getter @Setter private Set<String> idTokenEncryptionEncValuesSupported;

    /**
     * An array containing a list of supported JWS signing algorithms supported by Userinfo endpoint to encode claims
     */
    @JsonProperty("userinfo_signing_alg_values_supported")
    @Getter @Setter private Set<String> userinfoSigningAlgvaluesSupported;

    /**
     * An array containing a list of supported JWE encryption algorithms (alg value) supported by Userinfo endpoint to
     * encode claims
     */
    @JsonProperty("userinfo_encryption_alg_values_supported")
    @Getter @Setter private Set<String> userinfoEncryptionAlgValuesSupported;

    /**
     * An array containing a list of supported JWE encryption algorithms (enc value) supported by Userinfo endpoint to
     * encode claims
     */
    @JsonProperty("userinfo_encryption_enc_values_supported")
    @Getter @Setter private Set<String> userinfoEncryptionEncValuesSupported;

    /**
     * An array containing a list of supported JWS signing algorithms for request objects (by value and by reference)
     */
    @JsonProperty("request_object_signing_alg_values_supported")
    @Getter @Setter private Set<String> requestObjectSigningAlgValuesSupported;

    /**
     * An array containing a list of supported JWE encryption algorithms (alg value) for request objects
     */
    @JsonProperty("request_object_encryption_alg_values_supported")
    @Getter @Setter private Set<String> requestObjectEncryptionAlgValuesSupported;

    /**
     * An array containing a list of supported JWE encryption algorithms (enc value) for request objects
     */
    @JsonProperty("request_object_encryption_enc_values_supported")
    @Getter @Setter private Set<String> requestObjectEncryptionEncValuesSupported;

    /**
     * An array containing a list of supported authentication methods for the token endpoint
     * <p>
     *     Supported values are client_secret_post, client_secret_basic, client_secret_jwt, and private_key_jwt. The
     *     default value if not specified is client_secret_basic
     * </p>
     */
    @JsonProperty("token_endpoint_auth_methods_supported")
    @Getter @Setter private Set<String> tokenEndpointAuthMethodsSupported;

    /**
     * An array containing a list of supported JWS signing algorithms supported by the token endpoint for signature of JWT
     * used to authenticate the client at the token endpoint if the auth method is client_secret_jwt or private_key_jwt
     */
    @JsonProperty("token_endpoint_auth_signing_alg_values_supported")
    @Getter @Setter private Set<String> tokenEndpointAuthSigningAlgValuesSupported;

    /**
     * An array containing a list of valid "display" parameter values
     */
    @JsonProperty("display_values_supported")
    @Getter @Setter private Set<String> displayValuesSupported;

    /**
     * An array containing a list of supported claim types
     * <p>
     *     Supported values are normal, aggregated, and distributed. The default value if not specified is normal.
     * </p>
     */
    @JsonProperty("claim_types_supported")
    @Getter @Setter private Set<String> claimTypesSupported;

    /**
     * An array containing a list of claim names that might be supported. Other claims not in this list could be supported
     */
    @JsonProperty("claims_supported")
    @Getter @Setter private Set<String> claimsSupported;

    /**
     * The URL of a page containing documentation for necessary information for developers using this OpenID Connect Provider
     */
    @JsonProperty("service_documentation")
    @Getter @Setter private String serviceDocumentation;

    /**
     * An array of supported language tag values for values in claims being returned
     */
    @JsonProperty("claims_locales_supported")
    @Getter @Setter private String claimsLocalesSupported;

    /**
     * An array of supported language tag values by the user interface
     */
    @JsonProperty("ui_locales_supported")
    @Getter @Setter private String uiLocalesSupported;

    /**
     * True if claims parameter supported, false otherwise
     */
    @JsonProperty("claims_parameter_supported")
    @Getter @Setter private boolean claimsParameterSupported;

    /**
     * True if request parameter supported, false otherwise
     */
    @JsonProperty("request_parameter_supported")
    @Getter @Setter private boolean requestParameterSupported;

    /**
     * True if request_uri parameter supported, false otherwise
     */
    @JsonProperty("request_uri_parameter_supported")
    @Getter @Setter private boolean requestUriParameterSupported;

    /**
     * True if request_uri parameter values must be registered using the request_uris registration parameter, false otherwise
     */
    @JsonProperty("require_request_uri_registration")
    @Getter @Setter private boolean requireRequestUriRegistration;

    /**
     * The URL that the provider provides to the person registering the client to read about requirements on how the client
     * can use provided data
     */
    @JsonProperty("op_policy_uri")
    @Getter @Setter private String opPolicyUri;

    /**
     * The URL that the provider provides to the person registering the client to read about the terms of service
     */
    @JsonProperty("op_tos_uri")
    @Getter @Setter private String opTosUri;
}
