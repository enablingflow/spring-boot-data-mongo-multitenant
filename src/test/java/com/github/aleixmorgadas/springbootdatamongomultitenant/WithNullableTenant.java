package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document
@MultiTenant
public class WithNullableTenant {
    @Id
    ObjectId id;

    @MultiTenantField(value = "tenant", nullable = true)
    ObjectId tenant;

    public WithNullableTenant(ObjectId id, ObjectId tenant) {
        this.id = id;
        this.tenant = tenant;
    }
}
