package ca.bernstein.models.oauth;

import ca.bernstein.models.authentication.oidc.OidcResponseType;
import ca.bernstein.models.common.AuthorizationResponseType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A request object for an OAuth2.0 authorize request
 */
@ToString
public class OAuth2AuthorizationRequest {

    /**
     * The requested response type
     * <p>This can be a space separated list of response types</p>
     */
    @QueryParam("response_type")
    @Getter @Setter private String responseType;

    /**
     * The ID of the client requested authorization
     */
    @QueryParam("client_id")
    @Getter @Setter private String clientId;

    /**
     * A valid URI to redirect to upon completion of the request
     */
    @QueryParam("redirect_uri")
    @Getter @Setter private String redirectUri;

    /**
     * The requested scope of the authorization
     */
    @QueryParam("scope")
    @Getter @Setter private String scope;

    /**
     * Returns a set of appropriate response type implementation based on the response type field
     * <p>If no appropriate response types can be determined, return an empty set</p>
     * @return a set of new implementation of {@link AuthorizationResponseType}
     */
    public Set<AuthorizationResponseType> getResponseTypes() {

        if (StringUtils.isEmpty(responseType)) {
            return new HashSet<>();
        }

        return Stream.of(responseType.trim().split(" "))
                .map(responseTypeStr -> {
                    AuthorizationResponseType responseType = OAuth2ResponseType.fromString(responseTypeStr);
                    if (responseType == null) {
                        responseType = OidcResponseType.fromString(responseTypeStr);
                    }

                    return responseType;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
