package ca.bernstein.providers.mappers;

import ca.bernstein.exceptions.OAuth2Exception;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all OAuth2.0 web exceptions to a valid JSON response representation
 */
@Provider
public class OAuth2ExceptionMapper extends AbstractExceptionMapper<OAuth2Exception> {
}
