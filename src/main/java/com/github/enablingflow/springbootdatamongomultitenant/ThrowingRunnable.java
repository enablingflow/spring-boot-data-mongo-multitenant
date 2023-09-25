package com.github.enablingflow.springbootdatamongomultitenant;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Exception;
}
