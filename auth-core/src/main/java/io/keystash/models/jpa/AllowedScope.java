package io.keystash.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "allowed_scope")
public class AllowedScope implements Serializable {

    @Id
    @Column(name = "scope")
    @Getter @Setter private String scope;

}
