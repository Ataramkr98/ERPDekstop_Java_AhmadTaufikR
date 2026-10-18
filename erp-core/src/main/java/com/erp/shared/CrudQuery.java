package com.erp.shared;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalised list query: full-text-ish search, pagination, sorting and field filters.
 *
 * <p>PRD NFR-P-09 caps a single list at 200 rows, which {@code MAX_SIZE} enforces
 * regardless of what the client asks for.
 */
public class CrudQuery {

    public static final int MAX_SIZE = 200;
    public static final int DEFAULT_SIZE = 50;

    private final String search;
    private final int page;
    private final int size;
    private final String sort;
    private final boolean descending;
    private final Long companyId;
    private final Map<String, String> filters = new LinkedHashMap<>();
    private final boolean includeInactive;

    public CrudQuery(String search, Integer page, Integer size, String sort, Boolean descending,
                     Long companyId, Map<String, String> filters, Boolean includeInactive) {
        this.search = (search == null || search.isBlank()) ? null : search.trim();
        this.page = (page == null || page < 0) ? 0 : page;
        this.size = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        this.sort = (sort == null || sort.isBlank()) ? "id" : sort.trim();
        this.descending = descending == null ? true : descending;
        this.companyId = companyId;
        this.includeInactive = Boolean.TRUE.equals(includeInactive);
        if (filters != null) {
            this.filters.putAll(filters);
        }
    }

    public String search() {
        return search;
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public String sort() {
        return sort;
    }

    public boolean descending() {
        return descending;
    }

    public Long companyId() {
        return companyId;
    }

    public Map<String, String> filters() {
        return filters;
    }

    public boolean includeInactive() {
        return includeInactive;
    }
}
