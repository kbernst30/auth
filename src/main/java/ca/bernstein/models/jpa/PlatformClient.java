package ca.bernstein.models.jpa;

import ca.bernstein.models.oauth.OAuth2GrantType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@Entity(name = "platform_client")
public class PlatformClient implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, unique = true, name = "client_id")
    @Getter @Setter private String clientId;

    @Column(nullable = false, name = "client_secret")
    @Getter @Setter private String clientSecret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "account_id")
    @Getter @Setter private Account account;

    @Column(nullable = false, name = "authorities")
    @Getter @Setter private String authorities;

    @Column(nullable = false, name = "authorized_grant_types")
    @Getter @Setter private String authorizedGrantTypesStr;

    @Column(nullable = false, name = "scope")
    @Getter @Setter private String scope;

    @Column(nullable = false, name = "auto_approve")
    @Getter @Setter private boolean autoApprove;

    public Set<OAuth2GrantType> getAuthorizedGrantTypes() {
        return Stream.of(authorizedGrantTypesStr.split(","))
                .map(OAuth2GrantType::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void setAuthorizedGrantTypes(Set<OAuth2GrantType> grantTypes) {
        this.authorizedGrantTypesStr = String.join(",", grantTypes.stream()
                .map(OAuth2GrantType::name)
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
    }
}
