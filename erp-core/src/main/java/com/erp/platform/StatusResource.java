package com.erp.platform;

import java.util.Map;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/** Identifies this application's ready backend before a desktop reuses its port. */
@Path("/api/status") @Produces(MediaType.APPLICATION_JSON)
public class StatusResource {
    @Inject EntityManager em;
    @GET @PermitAll public Map<String,String> status() {
        em.createNativeQuery("select 1").getSingleResult();
        return Map.of("application","erp-workspace","status","UP");
    }
}
