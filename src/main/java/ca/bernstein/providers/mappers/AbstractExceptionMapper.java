package ca.bernstein.providers.mappers;

import ca.bernstein.exceptions.AbstractWebApplicationException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An abstract exception mapper
 */
@Slf4j
public abstract class AbstractExceptionMapper<T extends AbstractWebApplicationException> implements ExceptionMapper<T> {

    @Override
    public Response toResponse(T e) {
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
