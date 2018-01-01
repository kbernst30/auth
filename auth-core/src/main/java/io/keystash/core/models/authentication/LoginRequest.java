package io.keystash.core.models.authentication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.ws.rs.FormParam;

/**
 * A Request object to authenticate with the server
 */
@ToString
@EqualsAndHashCode
public class LoginRequest {

    /**
     * The unique identifying username of the user attempting authentication
     */
    @FormParam("username")
    @Getter @Setter private String username;

    /**
     * The unencrypted raw password provided by the authenticating user
     */
    @FormParam("password")
    @Getter @Setter private String password;

    /**
     * The URI to return to after authentication is processes
     */
    @FormParam("returnTo")
    @Getter @Setter private String returnTo;
}
