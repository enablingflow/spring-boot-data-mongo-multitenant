package com.github.aleixmorgadas.springbootdatamongomultitenant;

import java.util.function.Supplier;

public class MultiTenantContext {
    private static final ThreadLocal<Boolean> asRoot = new ThreadLocal<>();
    private static final ThreadLocal<String> asTenant = new ThreadLocal<>();
    private static final ThreadLocal<Supplier<String>> tenant = new ThreadLocal<>();

    public String get() {
        return tenant.get().get();
    }

    public void set(Supplier<String> value) {
        tenant.set(value);
    }

    public void remove() {
        tenant.remove();
    }

    void performAsRoot(Runnable runnable) {
        asRoot.set(true);
        try {
            runnable.run();
        } finally {
            asRoot.remove();
        }
    }

    void performAsTenant(String tenant, Runnable runnable) {
        asTenant.set(tenant);
        try {
            runnable.run();
        } finally {
            asTenant.remove();
        }
    }

    public boolean isRoot() {
        return asRoot.get() != null && asRoot.get();
    }

    public boolean hasScopedTenant() {
        return asTenant.get() != null;
    }

    public String getScopedTenant() {
        return asTenant.get();
    }
}
