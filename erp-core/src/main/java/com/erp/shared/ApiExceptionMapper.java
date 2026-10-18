package com.erp.shared;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

/**
 * Turns {@link ApiException} into a clean JSON body. Everything else is logged with a
 * correlation id and reported as a 500 with that id, never as raw stack-trace text.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(Throwable ex) {
        if (ex instanceof ApiException api) {
            return build(api.status, api.getMessage(), null);
        }
        if (ex instanceof jakarta.ws.rs.WebApplicationException web) {
            int status = web.getResponse().getStatus();
            return build(status, status == 400 ? "The request contains an invalid field value." : "Request rejected (" + status + ").", null);
        }
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof jakarta.persistence.OptimisticLockException)
                return build(409, "This record was changed by another operation. Refresh and try again.", null);
            if (cause instanceof org.hibernate.exception.ConstraintViolationException)
                return build(409, "A unique value already exists or a related record is missing. Check the record's references and code.", null);
            if (cause instanceof jakarta.validation.ConstraintViolationException)
                return build(400, "One or more required fields are invalid.", null);
        }
        String ref = "ERR-" + Long.toHexString(System.nanoTime()).toUpperCase();
        LOG.errorf(ex, "Unhandled failure %s", ref);
        return build(500, "The operation could not be completed. Reference: " + ref, ref);
    }

    private Response build(int status, String message, String ref) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        if (ref != null) {
            body.put("reference", ref);
        }
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
