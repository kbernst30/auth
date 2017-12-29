package io.keystash.common.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "allowed_scope")
public class ApplicationScope implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "application_id")
    @Getter @Setter private Application application;

    @Column(nullable = false, name = "scope")
    @Getter @Setter private String scope;


    @Column(nullable = false, name = "application_scope")
    @Getter @Setter private boolean applicationScope;

    @Column(nullable = false, name = "user_scope")
    @Getter @Setter private boolean userScope;
}
