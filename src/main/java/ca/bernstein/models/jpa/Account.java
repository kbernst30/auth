package ca.bernstein.models.jpa;

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
public class Account implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(nullable = false, unique = true, name = "email")
    @Getter @Setter private String email;

    @Column(name = "password")
    @Getter @Setter private String password;

    @Column(nullable = false, name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter private Date created;

    @Column(nullable = false, name = "verified")
    @Getter @Setter private boolean verified;

    @Column(name = "facebook_id")
    @Getter @Setter private String facebookId;
}
