# Spring Boot Data MongoDB Multi-Tenant

[![](https://jitpack.io/v/aleixmorgadas/spring-boot-data-mongo-multitenant.svg)](https://jitpack.io/#aleixmorgadas/spring-boot-data-mongo-multitenant)
![gradle workflow](https://github.com/aleixmorgadas/spring-boot-data-mongo-multitenant/actions/workflows/gradle.yml/badge.svg)

:warning: Working in progress

## Introduction

Support for multi-tenant repositories for Spring Boot Data MongoDB.

## Install

build.gradle.kts: 

```kotlin
repositories {
    mavenCentral() {
        content {
            excludeGroup("com.github.aleixmorgadas") // improves speed
        }
    }
    maven(url="https://jitpack.io") {
        content {
            includeGroup("com.github.aleixmorgadas")
        }
    }
}
dependencies {
    implementation("com.github.aleixmorgadas:spring-boot-data-mongo-multitenant:0.0.7")
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
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantContext;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantMongoRepository;


@Configuration
@EnableMongoRepositories(repositoryBaseClass = MultiTenantMongoRepository.class)
public class MongoConfig {
    @Bean
    MultiTenantContext multiTenantContext() {
        return new MultiTenantContext();
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MultiTenantContext multiTenantContext) {
        return new MultiTenantMongoTemplate(mongoDbFactory, multiTenantContext);
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

another example with `@DocumentReference`:

```java
@Document("teams")
@MultiTenant
public class Team {
    @Id
    ObjectId id;
    
    @DocumentReference
    @MultiTenantField("organization.id")
    Organization organization;
    // ...
}
```

## Example

Now, we could use the `BoardRepository` as a normal repository and when fetching data, the `tenantId` field will be used to filter the data:

```java
@DataMongoTest
@Testcontainers
public class MultiTenantTest {
    @ServiceConnection
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0.8");

    @Autowired
    private BoardRepository repository;

    @Autowired
    private MultiTenantContext multiTenantContext;

    @BeforeEach
    void setup() {
        repository.save(new Board(null, "board 1", "", "tenant-A"));
        repository.save(new Board(null, "board 2", "", "tenant-A"));
        repository.save(new Board(null, "board 3", "", "tenant-B"));
        repository.save(new Board(null, "board 4", "", "tenant-C"));
        multiTenantContext.set("tenant-A");
    }

    @AfterEach
    void clean() {
        multiTenantContext.performAsRoot(() -> repository.deleteAll());
    }

    @Test
    void receivesOnlyTenantA() {
        var users = repository.findAll();
        assert users.size() == 2;
    }

    @Test
    void receivesOnlyTenantA() {
        multiTenantContext.performAsRoot(() -> {
            var users = repository.findAll();
            assert users.size() == 4;
        });
        var users = multiTenantContext.performAsRoot(() -> repository.findAll());
        assert users.size() == 4;
    }

    @Test
    void receivesOnlyTenantA() {
        multiTenantContext.performAsTenant("tenant-B", () -> {
            var users = repository.findAll();
            assert users.size() == 1;
        });
        var users = multiTenantContext.performAsTenant("tenant-B", () -> repository.findAll());
        assert users.size() == 1;
    }
}
```


In case of an Entity annotated with `@MultiTenant` and `@Document` without a field with `@MultiTenantField`, it will **throw a run time exception**. 

```java

See [MultiTenantTest](./src/test/java/com/github/aleixmorgadas/springbootdatamongomultitenant/MultiTenantTest.java) for more examples.