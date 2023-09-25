package com.github.enablingflow.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@MultiTenant
public class TenantAsObjectId {
    @Id
    ObjectId id;

    @MultiTenantField(value = "tenantId")
    ObjectId tenantId;

    public TenantAsObjectId(ObjectId id, ObjectId tenantId) {
        this.id = id;
        this.tenantId = tenantId;
    }
}
