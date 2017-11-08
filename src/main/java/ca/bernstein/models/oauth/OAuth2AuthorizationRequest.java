package ca.bernstein.models.oauth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.ws.rs.QueryParam;

/**
 * A request object for an OAuth2.0 authorize request
 */
@ToString
public class OAuth2AuthorizationRequest {

    /**
     * The requested response type
     */
    @QueryParam("response_type")
    @Getter @Setter private OAuth2ResponseType responseType;

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

}
