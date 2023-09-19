package com.github.aleixmorgadas.springbootdatamongomultitenant;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MultiTenantContext {
    private final Map<String, Supplier<String>> tenantFields = new HashMap<>();

    public String get(String tenantField) {
        return tenantFields.get(tenantField).get();
    }

    public void register(String tenantField, Supplier<String> supplier) {
        tenantFields.put(tenantField, supplier);
    }
}
