package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiTenantTest {
    @ServiceConnection
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0.8");

    static {
        mongoContainer.start();
    }

    @Autowired
    private UserRepository repository;

    @Autowired
    private MissingAnnotationEntityRepository missingAnnotationEntityRepository;

    @Autowired
    private Map<String, Supplier<String>> tenants;

    @BeforeAll
    void setup() {
        repository.save(new User(null, "tenantA"));
        repository.save(new User(null, "tenantA"));
        repository.save(new User(null, "tenantB"));
        repository.save(new User(null, "tenantC"));
    }
    @Test
    @Order(1)
    void receivesOnlyTenantA() {
        tenants.put("default", () -> "tenantA");
        var users = repository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @Order(2)
    void deleteAllIsTenantAware() {
        repository.deleteAll();
        tenants.put("default", () -> "tenantB");
        var users = repository.findAll();
        assert users.size() == 1;
    }

    @Test
    @Order(3)
    void throwsExceptionWhenMultiTenantFilterIsNotPresentInAMultiTenantDocument() {
        var missingAnnotationEntity = new MissingAnnotationEntity();
        missingAnnotationEntityRepository.save(missingAnnotationEntity);
        assertThrows(RuntimeException.class, () -> missingAnnotationEntityRepository.findAll());
    }

    @Test
    @Order(4)
    void throwsExceptionWhenTenantDoesNotResolve() {
        tenants.put("default", () -> null);
        assertThrows(RuntimeException.class, () -> repository.findAll());
    }
}
