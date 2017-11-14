package ca.bernstein.providers;

import ca.bernstein.exceptions.web.LoginWebException;
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
                .entity(new Viewable("/login.jsp", e.getError()))
                .type(MediaType.TEXT_HTML)
                .build();
    }
}
