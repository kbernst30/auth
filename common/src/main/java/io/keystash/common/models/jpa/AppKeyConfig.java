package io.keystash.common.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "app_key_config")
@Table(name = "app_key_config")
public class AppKeyConfig implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "app_key_id")
    @Getter @Setter private AppKey appKey;

    @Column(nullable = false, name = "name")
    @Getter @Setter private String name;

    @Lob
    @Column(nullable = false, name = "value")
    @Getter @Setter private String value;
}
