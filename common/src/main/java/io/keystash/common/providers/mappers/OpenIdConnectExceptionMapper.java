package io.keystash.common.providers.mappers;

import io.keystash.common.exceptions.OpenIdConnectException;

import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that maps all OpenID Connect web exceptions to a valid JSON response representation
 */
@Provider
public class OpenIdConnectExceptionMapper extends AbstractExceptionMapper<OpenIdConnectException> {
}
