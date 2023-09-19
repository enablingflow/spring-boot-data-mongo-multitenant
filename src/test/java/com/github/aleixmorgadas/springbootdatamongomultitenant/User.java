package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@MultiTenant
public class User {
    @Id
    ObjectId id;

    @MultiTenantField(value = "tenantId")
    String tenantId;

    public User(ObjectId id, String tenantId) {
        this.id = id;
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
