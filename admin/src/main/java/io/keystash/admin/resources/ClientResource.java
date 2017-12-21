package io.keystash.admin.resources;

import io.keystash.admin.annotation.Authorization;
import io.keystash.admin.models.ResourceOwner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/clients")
public class ClientResource {

    @GET
    @Authorization(scope = "admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClients(@Context ResourceOwner resourceOwner) {
        System.out.println(resourceOwner);
        return Response.ok().build();
    }

}
