package io.keystash.core.resources.authentication;

import io.keystash.core.services.authentication.DiscoveryService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/.well-known")
public class DiscoveryResource {

    private final DiscoveryService discoveryService;

    @Inject
    public DiscoveryResource(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GET
    @Path("/openid-configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenIdConfiguration() {
        return Response.ok(discoveryService.discoverOpenIdConfiguration()).build();
    }

}
