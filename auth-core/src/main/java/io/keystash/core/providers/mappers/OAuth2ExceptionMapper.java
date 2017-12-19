package io.keystash.core.providers.mappers;

import io.keystash.core.exceptions.OAuth2Exception;

import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all OAuth2.0 web exceptions to a valid JSON response representation
 */
@Provider
public class OAuth2ExceptionMapper extends AbstractExceptionMapper<OAuth2Exception> {
}
