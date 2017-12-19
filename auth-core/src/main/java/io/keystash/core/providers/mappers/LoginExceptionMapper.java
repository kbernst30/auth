package io.keystash.core.providers.mappers;

import io.keystash.core.exceptions.LoginException;
import io.keystash.core.models.authentication.LoginPageConfig;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all login web exceptions to a error response to be returned to the login Viewable
 */
@Provider
public class LoginExceptionMapper implements ExceptionMapper<LoginException> {

    @Override
    public Response toResponse(LoginException e) {
        return Response.fromResponse(e.getResponse())
                .entity(new Viewable("/login.jsp", getLoginPageConfigForResponse(e)))
                .type(MediaType.TEXT_HTML)
                .build();
    }

    private LoginPageConfig getLoginPageConfigForResponse(LoginException e) {
        LoginPageConfig loginPageConfig = e.getLoginPageConfig();
        if (loginPageConfig == null) {
            loginPageConfig = new LoginPageConfig();
        }

        loginPageConfig.setError(e.getError() != null ? e.getError().getErrorDescription() : null);
        return loginPageConfig;
    }
}
