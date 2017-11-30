package ca.bernstein.models.authentication;

import lombok.*;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AuthenticatedUser implements Serializable {

    @Getter @Setter private int userId;
    @Getter @Setter private String email;

}
