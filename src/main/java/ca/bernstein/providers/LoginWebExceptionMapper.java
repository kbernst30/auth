package ca.bernstein.providers;

import ca.bernstein.exceptions.web.LoginWebException;
import ca.bernstein.models.authentication.LoginPageConfig;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all login web exceptions to a error response to be returned to the login Viewable
 */
@Provider
public class LoginWebExceptionMapper implements ExceptionMapper<LoginWebException> {

    @Override
    public Response toResponse(LoginWebException e) {
        return Response.fromResponse(e.getResponse())
                .entity(new Viewable("/login.jsp", getLoginPageConfigForResponse(e)))
                .type(MediaType.TEXT_HTML)
                .build();
    }

    private LoginPageConfig getLoginPageConfigForResponse(LoginWebException e) {
        LoginPageConfig loginPageConfig = e.getLoginPageConfig();
        if (loginPageConfig == null) {
            loginPageConfig = new LoginPageConfig();
        }

        loginPageConfig.setError(e.getError() != null ? e.getError().getErrorDescription() : null);
        return loginPageConfig;
    }
}
