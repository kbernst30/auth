package io.keystash.common.models.authentication;

import lombok.*;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AuthenticatedUser implements Serializable {

    @Getter @Setter private int userId;
    @Getter @Setter private String username;
    @Getter @Setter private int applicationId;

}
