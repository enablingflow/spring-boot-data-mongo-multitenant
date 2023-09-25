package com.github.enablingflow.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@MultiTenant
public class User {
    @Id
    ObjectId id;

    String name;

    @MultiTenantField(value = "tenantId")
    String tenantId;

    public User(ObjectId id, String name, String tenantId) {
        this.id = id;
        this.name = name;
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }

    public void setName(String s) {
        this.name = s;
    }
}
