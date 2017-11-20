package ca.bernstein.providers;

import ca.bernstein.exceptions.OAuth2Exception;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all OAuth2.0 web exceptions to a valid JSON response representation
 */
@Slf4j
@Provider
public class OAuth2ExceptionMapper implements ExceptionMapper<OAuth2Exception> {

    @Override
    public Response toResponse(OAuth2Exception e) {
        if (e.getResponse().getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            log.error(e.getMessage());
        } else {
            log.warn(e.getMessage());
        }

        return Response.fromResponse(e.getResponse())
                .entity(e.getError())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
