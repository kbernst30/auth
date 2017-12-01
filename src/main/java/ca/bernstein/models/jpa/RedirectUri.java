package ca.bernstein.models.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Entity(name = "redirect_uri")
public class RedirectUri implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(nullable = false, name = "platform_client_id")
    @Getter @Setter private PlatformClient platformClient;

    @Id
    @Getter @Setter private String value;

}
