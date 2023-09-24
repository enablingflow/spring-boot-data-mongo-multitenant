package com.github.aleixmorgadas.springbootdatamongomultitenant;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class MultiTenantContext<T> {
    private static final ThreadLocal<Boolean> asRoot = new ThreadLocal<>();
    private static final ThreadLocal<Object> asTenant = new ThreadLocal<>();
    private static final ThreadLocal<Object> tenant = new ThreadLocal<>();

    public T get() {
        return (T) tenant.get();
    }

    public void set(T value) {
        tenant.set(value);
    }

    public void remove() {
        tenant.remove();
    }

    public void performAsRoot(ThrowingRunnable runnable) throws Exception {
        asRoot.set(true);
        try {
            runnable.run();
        } finally {
            asRoot.remove();
        }
    }

    public <T> T performAsRoot(Callable<T> callable) throws Exception {
        asRoot.set(true);
        T result;
        try {
            result = callable.call();
        } finally {
            asRoot.remove();
        }
        return result;
    }

    public void performAsTenant(T tenant, ThrowingRunnable runnable) throws Exception {
        asTenant.set(tenant);
        try {
            runnable.run();
        } finally {
            asTenant.remove();
        }
    }

    public <T> T performAsTenant(String tenant, Callable<T> callable) throws Exception {
        asTenant.set(tenant);
        T result;
        try {
            result = callable.call();
        } finally {
            asTenant.remove();
        }
        return result;
    }

    public boolean isRoot() {
        return asRoot.get() != null && asRoot.get();
    }

    public boolean hasScopedTenant() {
        return asTenant.get() != null;
    }

    public T getScopedTenant() {
        return (T) asTenant.get();
    }
}
