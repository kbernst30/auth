package io.keystash.common.models.common;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

/**
 * Represents the values obtained in a basic auth header
 * <p>
 *     For the header to be properly processed, it must be in the following form:
 *
 *          Authorization: Basic Base64Encoded(clientId:clientSecret)
 * </p>
 */
@ToString
@EqualsAndHashCode
public class BasicAuthorizationDetails {

    /**
     * The client ID of the authorizing client
     */
    @Getter private String clientId;

    /**
     * The client secret of the authorizing client
     */
    @Getter private String clientSecret;

    private BasicAuthorizationDetails() {}

    public static BasicAuthorizationDetails fromHeaderString(String authorizationHeader) {
        BasicAuthorizationDetails basicAuthorizationDetails = new BasicAuthorizationDetails();
        if (!StringUtils.isEmpty(authorizationHeader)) {
            String[] headerParts = authorizationHeader.trim().split(" ");
            if (headerParts.length == 2 && headerParts[0].toLowerCase().equals("basic")) {
                String clientDetailsEncoded = headerParts[1];
                String clientDetailsDecoded = new String(Base64.getDecoder().decode(clientDetailsEncoded));
                String[] clientDetailsParts = clientDetailsDecoded.split(":");
                if (clientDetailsParts.length == 2) {
                    basicAuthorizationDetails.clientId = clientDetailsParts[0];
                    basicAuthorizationDetails.clientSecret = clientDetailsParts[1];
                }
            }
        }

        return basicAuthorizationDetails;
    }
}
