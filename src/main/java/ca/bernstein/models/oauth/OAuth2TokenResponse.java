package ca.bernstein.models.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class OAuth2TokenResponse {

    @JsonProperty("access_token")
    @Getter @Setter private String accessToken;

    @JsonProperty("expires_in")
    @Getter @Setter private int expiryTime;

    @JsonProperty("token_type")
    @Getter @Setter private String tokenType;

    @JsonProperty("scope")
    @Getter @Setter private String scope;

    @JsonProperty("refresh_token")
    @Getter @Setter private String refreshToken;

}
