package ca.bernstein.models.authentication;

import lombok.*;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class User {

    @Getter @Setter private int userId;
    @Getter @Setter private String username;

}
