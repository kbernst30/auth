package io.keystash.common.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "open_id_provider_config")
public class OpenIdProviderConfig implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, name = "name")
    @Getter @Setter private String name;

    @Lob
    @Column(nullable = false, name = "value")
    @Getter @Setter private String value;
}
