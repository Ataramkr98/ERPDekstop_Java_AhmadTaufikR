package com.erp.desktop;

import java.util.*;
import com.erp.platform.ModuleRegistry;

/** Navigation derives from the server registry so endpoint names cannot drift. */
public final class Catalog {
    private Catalog() {}
    public record Entry(String label, String path, String entity) {}
    public static List<String> modules() { return ModuleRegistry.ENTRIES.stream().map(ModuleRegistry.Entry::division).distinct().toList(); }
    public static String moduleLabel(String module) { return ModuleRegistry.ENTRIES.stream().filter(e -> e.division().equals(module)).map(ModuleRegistry.Entry::divisionLabel).findFirst().orElse(module); }
    public static List<Entry> entries(String module) { return ModuleRegistry.ENTRIES.stream().filter(e -> e.division().equals(module)).map(e -> new Entry(e.label(), e.path(), e.entity())).toList(); }
    public static int size() { return ModuleRegistry.ENTRIES.size(); }
}
