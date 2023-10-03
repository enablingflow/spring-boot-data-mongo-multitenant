package com.github.enablingflow.springbootdatamongomultitenant;

import java.util.LinkedList;
import java.util.concurrent.Callable;

public class MultiTenantContext<T> {
    private static final ThreadLocal<Boolean> asRoot = new ThreadLocal<>();
    private static final ThreadLocal<LinkedList<Object>> asTenant = new ThreadLocal<>();
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
        if (asRoot.get() != null && asRoot.get()) {
            runnable.run();
            return;
        }
        asRoot.set(true);
        try {
            runnable.run();
        } finally {
            asRoot.remove();
        }
    }

    public <T> T performAsRoot(Callable<T> callable) throws Exception {
        if (asRoot.get() != null && asRoot.get()) {
            return callable.call();
        }
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
        if (asTenant.get() == null) {
            asTenant.set(new LinkedList<>());
        }
        asTenant.get().add(tenant);
        try {
            runnable.run();
        } finally {
            asTenant.get().pollLast();
        }
    }

    public <T> T performAsTenant(String tenant, Callable<T> callable) throws Exception {
        if (asTenant.get() == null) {
            asTenant.set(new LinkedList<>());
        }
        asTenant.get().add(tenant);
        T result;
        try {
            result = callable.call();
        } finally {
            asTenant.get().pollLast();
        }
        return result;
    }

    public boolean isRoot() {
        return asRoot.get() != null && asRoot.get();
    }

    public boolean hasScopedTenant() {
        return asTenant.get() != null && !asTenant.get().isEmpty();
    }

    public T getScopedTenant() {
        return (T) asTenant.get().getLast();
    }
}
