package io.keystash.common.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@ToString
@EqualsAndHashCode
@Entity(name = "account")
@Table(name = "account")
public class Account implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, unique = true, name = "name")
    @Getter @Setter private String name;

    @Column(nullable = false, name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter private Date created;

}
