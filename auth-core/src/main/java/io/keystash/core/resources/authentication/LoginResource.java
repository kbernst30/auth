package io.keystash.core.resources.authentication;

import io.keystash.core.exceptions.authentication.AuthenticationException;
import io.keystash.core.exceptions.authentication.InvalidCredentialsException;
import io.keystash.core.exceptions.authentication.UnknownUserException;
import io.keystash.core.exceptions.LoginException;
import io.keystash.core.models.authentication.LoginPageConfig;
import io.keystash.core.models.authentication.LoginRequest;
import io.keystash.common.models.error.ErrorType;
import io.keystash.core.services.authentication.AuthenticationService;
import io.keystash.core.util.Validations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
    public Viewable getLoginForm(@QueryParam("returnTo") String returnTo) {
        return new Viewable("/login.jsp", getLoginPageConfigFromParams(returnTo));
    }

    /**
     * Processes a login request from the login form
     * @param loginRequest a request object containing necessary parameters needed for authentication
     * @return A Response that redirects to the last known location or to the main entry point for the application
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitLogin(@BeanParam LoginRequest loginRequest, @Context UriInfo uriInfo) {

        Validations.validateLoginRequest(loginRequest);

        try {
            authenticationService.login(loginRequest, uriInfo.getBaseUri().getHost());
            URI returnTo = getReturnToUri(loginRequest.getReturnTo());
            if (returnTo == null) {
                return Response.ok().build();
            }

            return Response.seeOther(returnTo).build();
        } catch (UnknownUserException e) {
            log.error("No user was found for username [{}]", loginRequest.getUsername(), e);
            throw new LoginException(ErrorType.Authentication.UNKNOWN_USER, Response.Status.BAD_REQUEST,
                    getLoginPageConfigFromLoginRequest(loginRequest), loginRequest.getUsername());

        } catch (InvalidCredentialsException e) {
            log.error("Invalid credentials given for username [{}]", loginRequest.getUsername(), e);
            throw new LoginException(ErrorType.Authentication.INVALID_CREDENTIALS, Response.Status.BAD_REQUEST,
                    getLoginPageConfigFromLoginRequest(loginRequest));

        } catch (AuthenticationException e) {
            log.error("An unknown error occurred attempting authentication for username [{}]", loginRequest.getUsername(), e);
            throw new LoginException(ErrorType.Authentication.SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR,
                    getLoginPageConfigFromLoginRequest(loginRequest));
        }
    }

    private URI getReturnToUri(String returnTo) {
        try {
            return StringUtils.isEmpty(returnTo) || returnTo.equals("null") ? null : new URI(returnTo);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private LoginPageConfig getLoginPageConfigFromParams(String returnTo) {
        LoginPageConfig loginPageConfig = new LoginPageConfig();
        loginPageConfig.setReturnTo(returnTo);
        return loginPageConfig;
    }

    private LoginPageConfig getLoginPageConfigFromLoginRequest(LoginRequest loginRequest) {
        LoginPageConfig loginPageConfig = new LoginPageConfig();
        loginPageConfig.setReturnTo(loginRequest.getReturnTo());
        return loginPageConfig;
    }
}
