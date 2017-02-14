package org.apache.brooklyn.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.brooklyn.rest.domain.Credential;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("Authentication")
@Path("/auth-api")
@Produces(MediaType.APPLICATION_JSON)
public interface AuthenticationApi {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/login")
    @ApiOperation(
            value = "login"
    )
    Response formLogin(@FormParam("username") final String username, @FormParam("password") String password);
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    Response authenticateUser(Credential credential);

}
