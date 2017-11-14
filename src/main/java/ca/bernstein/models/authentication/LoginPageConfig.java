package ca.bernstein.models.authentication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A configuration object for the login page that will determine specific login behaviour
 */
@ToString
@EqualsAndHashCode
public class LoginPageConfig {

    /**
     * The URI to return to after successful login
     */
    @Getter @Setter private String returnTo;

    /**
     * An error generated from an unsuccessful login attempt
     */
    @Getter @Setter private String error;

}
