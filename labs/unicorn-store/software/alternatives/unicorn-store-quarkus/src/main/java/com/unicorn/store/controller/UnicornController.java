package com.unicorn.store.controller;

import com.unicorn.store.exceptions.ResourceNotFoundException;
import com.unicorn.store.model.Unicorn;
import com.unicorn.store.service.UnicornService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
public class UnicornController {

    private static final Logger logger = LoggerFactory.getLogger(UnicornController.class);

    @Inject
    UnicornService unicornService;

    public UnicornController() {
    }

    public UnicornController(UnicornService unicornService) {
        this.unicornService = unicornService;
    }

    @POST
    @Path("/unicorns")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUnicorn(Unicorn unicorn) {
        try {
            var savedUnicorn = unicornService.createUnicorn(unicorn);
            return Response.ok(savedUnicorn).build();
        } catch (Exception e) {
            String errorMsg = "Error creating unicorn";
            logger.error(errorMsg, e);
            throw new ProcessingException(errorMsg, e);
        }
    }

    @PUT
    @Path("/unicorns/{unicornId}")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUnicorn(Unicorn unicorn, @PathParam("unicornId") String unicornId) {
        try {
            var savedUnicorn = unicornService.updateUnicorn(unicorn, unicornId);
            return Response.ok(savedUnicorn).build();
        } catch (Exception e) {
            String errorMsg = "Error updating unicorn";
            logger.error(errorMsg, e);
            throw new NotFoundException(errorMsg, e);
        }
    }

    @GET()
    @Path("/unicorns/{unicornId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnicorn(@PathParam("unicornId") String unicornId) {
        try {
            System.out.println("unicornId: " + unicornId);
            var unicorn = unicornService.getUnicorn(unicornId);
            return Response.ok(unicorn).build();
        } catch (ResourceNotFoundException e) {
            String errorMsg = "Unicorn not found";
            logger.error(errorMsg, e);
            throw new NotFoundException(errorMsg, e);
        }
    }

    @DELETE
    @Path("/unicorns/{unicornId}")
    public Response deleteUnicorn(@PathParam("unicornId") String unicornId) {
        try {
            unicornService.deleteUnicorn(unicornId);
            return Response.ok().build();
        } catch (ResourceNotFoundException e) {
            String errorMsg = "Unicorn not found";
            logger.error(errorMsg, e);
            throw new NotFoundException(errorMsg, e);
        }
    }
}
