package ca.bernstein.models.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonIgnore
    public String getAsUriFragment() {
        StringBuilder builder = new StringBuilder()
                .append("access_token=").append(accessToken)
                .append("&expires_in=").append(expiryTime)
                .append("&token_type=").append(tokenType)
                .append("&scope=").append(scope);

        if (refreshToken != null) {
            builder.append("&refresh_token=").append(refreshToken);
        }

        return builder.toString();
    }

}
