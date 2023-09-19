package com.github.aleixmorgadas.springbootdatamongomultitenant;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Exception;
}
