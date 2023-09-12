# Spring Boot Data MongoDB Multi-Tenant

:warning: Working in progress

## Introduction

Support for multi-tenant repositories for Spring Boot Data MongoDB.

## Install

build.gradle.kts: 

```kotlin
repositories {
    mavenCentral()
    maven(url="https://jitpack.io")
}
dependencies {
    implementation("com.github.aleixmorgadas:spring-boot-data-mongodb-multitenant:main-SNAPSHOT")
}
```

## Configure

You need to use the `MultiTenantMongoTemplate` in order to support the multi-tenant repositories:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MultiTenantMongoTemplate;

import java.util.Map;

@Configuration
public class MongoConfig {

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
        var filters = Map.of("", () -> "tenantId");
        return new MultiTenantMongoTemplate(mongoDbFactory, filters);
    }
}
```

## Usage

Mark your entity with `@MultiTenant` annotation and mark the field that will be used to filter the data with `@MultiTenantField` annotation:

```java
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenant;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantField;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.bson.types.ObjectId;

@Document
@MultiTenant
class Board {
    @Id
    private String id;
    private String name;
    private String description;
    @MultiTenantField("tenantId")
    private String tenantId;
    // ...
}
```


