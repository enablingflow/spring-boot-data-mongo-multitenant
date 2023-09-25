package com.github.enablingflow.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
