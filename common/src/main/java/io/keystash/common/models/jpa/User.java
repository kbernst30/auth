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
@Entity(name = "user")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "username"}), name = "user")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "application_id")
    @Getter @Setter private Application application;

    @Column(nullable = false, name = "username")
    @Getter @Setter private String username;

    @Column(name = "password")
    @Getter @Setter private String password;

    @Column(nullable = false, name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter private Date created;

    @Column(name = "last_active")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter private Date lastActive;

    @Column(nullable = false, name = "verified")
    @Getter @Setter private boolean verified;

}
