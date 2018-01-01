package io.keystash.common.models.jpa;

import io.keystash.common.models.oauth.OAuth2GrantType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@Entity(name = "client")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "client_id"}), name = "client")
public class Client implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, name = "client_id")
    @Getter @Setter private String clientId;

    @Column(nullable = false, name = "client_secret")
    @Getter @Setter private String clientSecret;

    @ManyToOne
    @JoinColumn(nullable = false, name = "application_id")
    @Getter @Setter private Application application;

    @Column(nullable = false, name = "authorized_grant_types")
    @Getter @Setter private String authorizedGrantTypesStr;

    @Column(nullable = false, name = "scope")
    @Getter @Setter private String scope;

    @Column(nullable = false, name = "auto_approve")
    @Getter @Setter private boolean autoApprove;

    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Getter @Setter private List<RedirectUri> redirectUris;

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
