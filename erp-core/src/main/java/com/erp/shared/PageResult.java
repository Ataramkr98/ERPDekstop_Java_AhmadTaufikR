package com.erp.shared;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope returned by every list endpoint so the UI can page without a second round trip.
 */
public class PageResult<T> {

    public List<T> items;
    public long total;
    public int page;
    public int size;
    public int pages;

    public PageResult() {
    }

    public PageResult(List<T> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        PageResult<R> out = new PageResult<>();
        out.items = items.stream().map(mapper).toList();
        out.total = total;
        out.page = page;
        out.size = size;
        out.pages = pages;
        return out;
    }
}
