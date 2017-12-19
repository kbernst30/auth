package io.keystash.admin.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/clients")
public class ClientResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClients() {
        return Response.ok().build();
    }

}
