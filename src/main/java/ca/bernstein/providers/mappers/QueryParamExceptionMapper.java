package ca.bernstein.providers.mappers;

import ca.bernstein.models.error.ErrorResponse;
import ca.bernstein.models.error.ErrorType;
import org.glassfish.jersey.server.ParamException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that takes care of invalid query params and maps to an appropriate server error
 */
@Provider
public class QueryParamExceptionMapper implements ExceptionMapper<ParamException.QueryParamException> {

    @Override
    public Response toResponse(ParamException.QueryParamException e) {
        return Response.serverError()
                .entity(new ErrorResponse(ErrorType.OAuth2.SERVER_ERROR, e.getParameterName()))
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
