package ca.bernstein.resources.authentication;

import ca.bernstein.exceptions.authentication.AuthenticationException;
import ca.bernstein.exceptions.authentication.InvalidCredentialsException;
import ca.bernstein.exceptions.authentication.UnknownAccountException;
import ca.bernstein.exceptions.web.LoginWebException;
import ca.bernstein.models.authentication.LoginRequest;
import ca.bernstein.models.error.ErrorType;
import ca.bernstein.services.authentication.AuthenticationService;
import ca.bernstein.util.Validations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A resource for Login requests
 */
@Slf4j
@Path("login")
public class LoginResource {

    private final AuthenticationService authenticationService;

    @Inject
    public LoginResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Gets the appropriate login form for a user to interface with
     * <p>The form will have only the valid options that are configured with the application (i.e. remember me functionality)</p>
     * @return A Viewable object wrapping the login.jsp
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLoginForm() {
        return new Viewable("/login.jsp");
    }

    /**
     * Processes a login request from the login form
     * @param loginRequest a request object containing necessary parameters needed for authentication
     * @return A Response that redirects to the last known location or to the main entry point for the application
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitLogin(@BeanParam LoginRequest loginRequest) {

        Validations.validateLoginRequest(loginRequest);

        try {
            authenticationService.login(loginRequest);
            URI returnTo = getReturnToUri(loginRequest.getReturnTo());
            if (returnTo == null) {
                return Response.ok().build();
            }

            return Response.temporaryRedirect(returnTo).build();
        } catch (UnknownAccountException e) {
            log.error("No account was found for email [{}]", loginRequest.getUsername(), e);
            throw new LoginWebException(ErrorType.Authentication.UNKNOWN_ACCOUNT, Response.Status.BAD_REQUEST,
                    loginRequest.getUsername());
        } catch (InvalidCredentialsException e) {
            log.error("Invalid credentials given for email [{}]", loginRequest.getUsername(), e);
            throw new LoginWebException(ErrorType.Authentication.INVALID_CREDENTIALS, Response.Status.BAD_REQUEST);
        } catch (AuthenticationException e) {
            log.error("An unknown error occurred attempting authentication for email [{}]", loginRequest.getUsername(), e);
            throw new LoginWebException(ErrorType.Authentication.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private URI getReturnToUri(String returnTo) {
        try {
            return new URI(returnTo);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
