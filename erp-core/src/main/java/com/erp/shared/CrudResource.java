package com.erp.shared;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.erp.config.CurrentUser;
import com.erp.platform.AuditService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Base class for every REST resource in the platform.
 *
 * <p>A concrete module resource is usually three lines:
 * <pre>{@code
 *   @Path("/api/vendors")
 *   public class VendorResource extends CrudResource<Vendor> {
 *       protected Class<Vendor> entityType() { return Vendor.class; }
 *   }
 * }</pre>
 * and immediately gains list (search + filter + sort + page), get, create, update,
 * soft-delete, a field-metadata descriptor the UI renders from, and CSV export/import.
 *
 * <p>JAX-RS annotation inheritance is relied on deliberately: subclasses carry only the
 * class-level {@code @Path} and no method-level annotations, so the annotations defined
 * here on the superclass methods are inherited per the JAX-RS spec.
 *
 * <p>Entities intentionally hold foreign keys as plain {@code Long} fields rather than
 * {@code @ManyToOne} associations. That keeps Jackson serialization cycle-free, keeps
 * CSV import/export trivial, and - critically for PRD NFR-P-03 - stops an innocent
 * list query from dragging half the schema in through lazy loading.
 */
public abstract class CrudResource<T extends BaseEntity> {

    @Inject
    CrudService crud;

    @Inject
    CurrentUser currentUser;

    @Inject
    AuditService audit;
    @Inject ReferenceValidation references;

    // ------------------------------------------------------- contract to fill

    protected abstract Class<T> entityType();

    /** Free-text search across these (String-typed) fields. */
    protected String[] searchFields() {
        return new String[] { "code", "name" };
    }

    /** Reference data such as Currency is shared across companies. */
    protected boolean companyScoped() {
        return true;
    }

    protected String defaultSort() {
        return "id";
    }

    /** Override to add module validation; throw {@link ApiException} to reject. */
    protected void beforeSave(T entity, boolean isNew) {
    }

    protected void afterSave(T entity, boolean isNew) {
    }

    protected void beforeDelete(T entity) {
    }

    // ---------------------------------------------------------------- queries

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PageResult<T> list(@Context UriInfo uriInfo,
                              @QueryParam("q") String q,
                              @QueryParam("page") @DefaultValue("0") Integer page,
                              @QueryParam("size") @DefaultValue("50") Integer size,
                              @QueryParam("sort") String sort,
                              @QueryParam("desc") @DefaultValue("true") Boolean desc,
                              @QueryParam("includeInactive") @DefaultValue("false") Boolean includeInactive) {
        RecordPolicy.access(entityType(), currentUser, false);
        return crud.search(entityType(), buildQuery(uriInfo, q, page, size, sort, desc, includeInactive),
                searchFields());
    }

    @GET
    @Path("lookup")
    @Produces(MediaType.APPLICATION_JSON)
    public List<T> lookup(@QueryParam("q") String q,
                          @QueryParam("size") @DefaultValue("20") Integer size) {
        RecordPolicy.access(entityType(), currentUser, false);
        CrudQuery query = new CrudQuery(q, 0, Math.min(size, 100), "id", false,
                companyIdOrNull(), Map.of(), false);
        return crud.findAll(entityType(), query, searchFields());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public T get(@PathParam("id") Long id) {
        RecordPolicy.access(entityType(), currentUser, false);
        return owned(id);
    }

    // ------------------------------------------------------------------ write

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(T body) {
        RecordPolicy.access(entityType(), currentUser, true);
        if (body == null) {
            throw ApiException.badRequest("No payload was sent.");
        }
        body.id = null;
        body.version = null;
        body.createdBy = currentUser.username();
        body.updatedBy = currentUser.username();
        body.active = true;
        applyTenancy(body, true);
        RecordPolicy.editable(body);
        RecordPolicy.preserveCalculated(body, null);
        beforeSave(body, true);
        EntityValidation.validate(body);
        references.validate(body);
        T saved = crud.save(body);
        afterSave(saved, true);
        audit.created(saved);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public T update(@PathParam("id") Long id, T body) {
        RecordPolicy.access(entityType(), currentUser, true);
        if (body == null) throw ApiException.badRequest("No payload was sent.");
        T existing = owned(id);
        RecordPolicy.editable(existing);
        RecordPolicy.editable(body);
        if (body.version == null || !body.version.equals(existing.version))
            throw ApiException.conflict("This record changed since you opened it. Refresh and try again.");
        String before = AuditService.Snapshot.of(existing);
        body.id = id;
        body.createdAt = existing.createdAt;
        body.createdBy = existing.createdBy;
        body.active = existing.active;
        body.companyId = existing.companyId;
        RecordPolicy.preserveCalculated(body, existing);
        applyTenancy(body, false);
        beforeSave(body, false);
        EntityValidation.validate(body);
        references.validate(body);
        T saved = crud.save(body);
        afterSave(saved, false);
        audit.updated(entityType().getSimpleName(), id, before, AuditService.Snapshot.of(saved));
        return saved;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        RecordPolicy.access(entityType(), currentUser, true);
        T existing = owned(id);
        RecordPolicy.editable(existing);
        beforeDelete(existing);
        references.validate(existing);
        String before = AuditService.Snapshot.of(existing);
        crud.softDelete(existing);
        audit.deleted(entityType().getSimpleName(), id, before);
        return Response.noContent().build();
    }

    // ------------------------------------------------------- metadata + files

    /**
     * Field descriptor the UI builds its grid and form from, so adding an entity
     * never requires a matching UI change.
     *
     * <p>Requires a session on purpose: the descriptor enumerates the full field set,
     * types, lengths and enum constants of every entity in the platform, which is a
     * schema map that has no business being readable without authenticating.
     */
    @GET
    @Path("meta")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Map<String, Object> meta() {
        RecordPolicy.access(entityType(), currentUser, false);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("entity", entityType().getSimpleName());
        out.put("searchFields", searchFields());
        out.put("companyScoped", tenantScoped());
        out.put("readOnly", RecordPolicy.readOnly(entityType(), currentUser));
        out.put("readOnlyReason", RecordPolicy.readOnlyReason(entityType(), currentUser));
        out.put("importable", !entityType().getSimpleName().equals("AppUser"));

        List<Map<String, Object>> fields = new ArrayList<>();
        T defaults;
        try { defaults = entityType().getDeclaredConstructor().newInstance(); }
        catch (ReflectiveOperationException ex) { throw new IllegalStateException("Cannot read entity defaults", ex); }
        for (Field f : CrudService.dataFields(entityType())) {
            String name = f.getName();
            if (RecordPolicy.sensitive(name) || name.equals("createdAt") || name.equals("createdBy")
                    || name.equals("updatedAt") || name.equals("updatedBy")
                    || name.equals("active") || name.equals("version")) {
                continue;
            }
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("name", name);
            meta.put("label", humanise(name));
            meta.put("type", uiType(f.getType()));
            meta.put("readOnly", !RecordPolicy.writable(f));
            jakarta.persistence.Column column = f.getAnnotation(jakarta.persistence.Column.class);
            meta.put("required", column != null && !column.nullable() && RecordPolicy.writable(f));
            meta.put("maxLength", column != null && f.getType() == String.class ? column.length() : 0);
            meta.put("javaType", f.getType().getSimpleName());
            try {
                Object value = f.get(defaults);
                if (value != null) meta.put("defaultValue", value.toString());
            } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
            ReferenceValidation.target(entityType(), name).ifPresent(target -> {
                meta.put("referencePath", target.path());
                meta.put("referenceLabel", target.label());
            });
            if (f.getType().isEnum()) {
                List<String> values = new ArrayList<>();
                for (Object c : f.getType().getEnumConstants()) {
                    values.add(((Enum<?>) c).name());
                }
                meta.put("enumValues", values);
            }
            fields.add(meta);
        }
        out.put("fields", fields);
        if (entityType().getSimpleName().equals("AppUser")) fields.add(Map.of("name", "password", "label", "New password", "type", "password", "readOnly", false));
        return out;
    }

    @GET
    @Path("export.csv")
    @Produces("text/csv; charset=UTF-8")
    public String exportCsv(@Context UriInfo uriInfo,
                            @QueryParam("q") String q,
                            @QueryParam("size") @DefaultValue("200") Integer size,
                            @QueryParam("sort") String sort,
                            @QueryParam("desc") @DefaultValue("true") Boolean desc) {
        RecordPolicy.access(entityType(), currentUser, false);
        int limit = 5000;
        List<T> rows = new ArrayList<>();
        for (int page = 0; ; page++) {
            CrudQuery query = new CrudQuery(q, page, 200, sort == null ? defaultSort() : sort, desc,
                    companyIdOrNull(), filtersFrom(uriInfo), false);
            PageResult<T> result = crud.search(entityType(), query, searchFields());
            if (result.total > limit) throw ApiException.badRequest("Export supports up to 5,000 records. Narrow your search or filters.");
            rows.addAll(result.items);
            if (rows.size() >= result.total || result.items.isEmpty()) break;
        }

        List<Field> cols = CsvSupport.exportFields(entityType());
        List<String> header = cols.stream().map(Field::getName).toList();
        List<List<Object>> body = new ArrayList<>();
        for (T row : rows) {
            List<Object> cells = new ArrayList<>();
            for (Field f : cols) {
                try {
                    cells.add(f.get(row));
                } catch (ReflectiveOperationException e) {
                    cells.add(null);
                }
            }
            body.add(cells);
        }
        return CsvSupport.render(header, body);
    }

    /** Header row names entity fields; unknown columns fail the atomic import. */
    @POST
    @Path("import")
    @Consumes({ "text/csv", "text/plain" })
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Map<String, Object> importCsv(String body) {
        RecordPolicy.access(entityType(), currentUser, true);
        if (entityType().getSimpleName().equals("AppUser")) {
            throw ApiException.badRequest("Create accounts with the account form and its password field.");
        }
        if (body == null || body.length() > 4_000_000) throw ApiException.badRequest("CSV must be smaller than 4 MB.");
        List<List<String>> rows = CsvSupport.parse(body);
        if (rows.size() < 2) throw ApiException.badRequest("The CSV needs a header and at least one data row.");
        if (rows.size() > 1001) throw ApiException.badRequest("Import supports up to 1,000 records at a time.");
        List<String> header = rows.get(0).stream().map(String::trim).toList();
        if (new java.util.HashSet<>(header).size() != header.size()) throw ApiException.badRequest("CSV contains duplicate headers.");
        for (String name : header) if (field(entityType(), name) == null)
            throw ApiException.badRequest("Unknown CSV column: " + name + ". Use exported field names.");
        List<T> pending = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> cells = rows.get(i);
            if (cells.stream().allMatch(String::isBlank)) continue;
            if (cells.size() != header.size()) throw ApiException.badRequest("Row " + (i + 1) + " has a different number of columns.");
            try {
                T entity = entityType().getDeclaredConstructor().newInstance();
                for (int c = 0; c < header.size(); c++) {
                    Field f = field(entityType(), header.get(c));
                    if (!RecordPolicy.writable(f)) continue;
                    String raw = cells.get(c);
                    if (raw.isBlank()) continue;
                    if (f.getType() == String.class && raw.matches("^'[=+@-].*")) raw = raw.substring(1);
                    Object value = CrudService.coerce(raw, f.getType());
                    if (value == null) throw ApiException.badRequest(header.get(c) + " has an invalid value.");
                    f.set(entity, value);
                }
                applyTenancy(entity, true);
                RecordPolicy.editable(entity);
                RecordPolicy.preserveCalculated(entity, null);
                EntityValidation.validate(entity);
                pending.add(entity);
            } catch (ApiException ex) {
                throw ApiException.badRequest("Row " + (i + 1) + ": " + ex.getMessage() + " No records were imported.");
            } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
        }
        if (pending.isEmpty()) throw ApiException.badRequest("The CSV has no data rows.");
        for (T entity : pending) {
            beforeSave(entity, true);
            EntityValidation.validate(entity);
            references.validate(entity);
            T saved = crud.save(entity);
            afterSave(saved, true);
            audit.created(saved);
        }
        return Map.of("created", pending.size(), "errors", List.of());
    }
    // ---------------------------------------------------------------- helpers

    protected Long companyIdOrNull() {
        if (!tenantScoped()) return null;
        Long company = currentUser.companyId();
        if (company == null) throw ApiException.forbidden("Sign in to an active company.");
        return company;
    }

    private T owned(Long id) {
        T entity = crud.get(entityType(), id);
        if (tenantScoped()) RecordPolicy.owned(entity, companyIdOrNull());
        return entity;
    }

    protected CrudQuery buildQuery(UriInfo uriInfo, String q, Integer page, Integer size,
                                   String sort, Boolean desc, Boolean includeInactive) {
        return new CrudQuery(q, page, size, sort == null ? defaultSort() : sort, desc,
                companyIdOrNull(), filtersFrom(uriInfo), includeInactive);
    }

    private static Map<String, String> filtersFrom(UriInfo uriInfo) {
        Map<String, String> out = new LinkedHashMap<>();
        if (uriInfo == null) {
            return out;
        }
        for (Map.Entry<String, List<String>> e : uriInfo.getQueryParameters().entrySet()) {
            String key = e.getKey();
            if (key.startsWith("f.")) {
                out.put(key.substring(2), e.getValue().get(0));
            } else if (key.startsWith("filter_")) {
                out.put(key.substring(7), e.getValue().get(0));
            }
        }
        return out;
    }

    private void applyTenancy(T entity, boolean isNew) {
        if (tenantScoped()) {
            Long companyId = currentUser.companyId();
            if (companyId != null) {
                entity.companyId = companyId;
            }
        }
        if (isNew && entity.active == null) {
            entity.active = Boolean.TRUE;
        }
    }

    /** Demo credentials may never enumerate or edit shared rows outside their company. */
    private boolean tenantScoped() {
        return companyScoped() || currentUser.isDemo();
    }

    private static Field field(Class<?> cls, String name) {
        return CrudService.dataFields(cls).stream()
                .filter(f -> f.getName().equals(name))
                .findFirst().orElse(null);
    }

    private static String uiType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }
        if (type == Long.class || type == Integer.class || type == long.class || type == int.class) {
            return "integer";
        }
        if (type == java.math.BigDecimal.class || type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "decimal";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        }
        if (type == LocalDate.class || type == Instant.class || type == java.time.LocalDateTime.class) {
            return "date";
        }
        if (type.isEnum()) {
            return "enum";
        }
        return "string";
    }

    private static String humanise(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(' ').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
