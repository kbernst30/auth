package io.keystash.common.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "application")
public class Application implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, unique = true, name = "name")
    @Getter @Setter private String name;

    @Column(nullable = false, unique = true, name = "host_name")
    @Getter @Setter private String hostName;

}
