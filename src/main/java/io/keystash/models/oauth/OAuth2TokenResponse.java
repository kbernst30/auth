package io.keystash.models.oauth;

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
    @Getter @Setter private Integer expiryTime;

    @JsonProperty("token_type")
    @Getter @Setter private String tokenType;

    @JsonProperty("scope")
    @Getter @Setter private String scope;

    @JsonProperty("state")
    @Getter @Setter private String state;

    @JsonProperty("refresh_token")
    @Getter @Setter private String refreshToken;

    @JsonProperty("id_token")
    @Getter @Setter private String idToken;

    @JsonIgnore
    public String getAsUrlEncodedFormParams() {
        StringBuilder builder = new StringBuilder().append("token_type=").append(tokenType);

        if (accessToken != null) {
            builder.append("&access_token=").append(accessToken)
                    .append("&expires_in=").append(expiryTime)
                    .append("&scope=").append(scope);
        }

        if (state != null) {
            builder.append("&state=").append(state);
        }

        if (refreshToken != null) {
            builder.append("&refresh_token=").append(refreshToken);
        }

        if (idToken != null) {
            builder.append("&id_token=").append(idToken);
        }

        return builder.toString();
    }

}
