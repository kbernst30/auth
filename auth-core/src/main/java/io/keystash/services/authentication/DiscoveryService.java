package io.keystash.services.authentication;

import io.keystash.models.authentication.OpenIdProviderMetadata;

/**
 * Service to provide discovery information about the OpenID Connect Provider
 * <p>
 *     @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html">
 *          http://openid.net/specs/openid-connect-discovery-1_0.html
 *         </a>
 * </p>
 */
public interface DiscoveryService {

    /**
     * Discovers and returns the configuration of the OpenID Connect Provider (OP)
     * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html#rfc.section.4">
     *     http://openid.net/specs/openid-connect-discovery-1_0.html#rfc.section.4
     *     </a>
     * @return an object representing the configuration of the OP
     */
    OpenIdProviderMetadata discoverOpenIdConfiguration();

}
