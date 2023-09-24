package com.github.aleixmorgadas.springbootdatamongomultitenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiTenantField {
    String value() default "";

    boolean nullable() default false;
}
