package io.keystash.models.jpa;

import io.keystash.models.jose.JwsAlgorithmType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@ToString
@EqualsAndHashCode
@Entity(name = "app_key")
public class AppKey implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, name = "name")
    @Getter @Setter private String name;

    @Column(nullable = false, name = "algorithm")
    @Getter private String algorithm;

    @OneToMany(mappedBy = "appKey", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Getter @Setter private List<AppKeyConfig> configs;

    public void setAlgorithm(JwsAlgorithmType algorithmType) {
        this.algorithm = algorithmType.toString();
    }
}
