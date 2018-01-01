package io.keystash.common.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "redirect_uri")
@Table(name = "redirect_uri")
public class RedirectUri implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(nullable = false, name = "client_id")
    @Getter @Setter private Client client;

    @Id
    @Getter @Setter private String value;

}
