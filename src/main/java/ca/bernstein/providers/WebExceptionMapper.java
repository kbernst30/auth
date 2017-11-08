package ca.bernstein.providers;

import ca.bernstein.exceptions.OAuth2WebException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all web exceptions to a valid JSON response representation
 */
@Provider
public class WebExceptionMapper implements ExceptionMapper<OAuth2WebException> {

    @Override
    public Response toResponse(OAuth2WebException e) {
        return Response.fromResponse(e.getResponse())
                .entity(e.getError())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
