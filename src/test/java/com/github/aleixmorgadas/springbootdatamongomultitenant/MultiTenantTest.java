package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = MultiTenantTestConfig.class)
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
    private MultiTenantContext tenants;

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
        tenants.set(() -> "tenantA");
        var users = repository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @Order(2)
    void performAsTenant() throws Exception {
        tenants.performAsTenant("tenantC", () -> {
            var users = repository.findAll();
            assertEquals(1, users.size());
        });

        var tenant = tenants.performAsTenant("tenantC", () -> repository.findAll());
        assertEquals(1, tenant.size());
    }

    @Test
    @Order(3)
    void receivesAllAsRoot() throws Exception {
        tenants.performAsRoot(() -> {
            var users = repository.findAll();
            assertEquals(4, users.size());
        });

        var users = tenants.performAsRoot(() -> repository.findAll());
        assertEquals(4, users.size());
    }

    @Test
    @Order(4)
    void deleteAllIsTenantAware() {
        repository.deleteAll();
        tenants.set(() -> "tenantB");
        var users = repository.findAll();
        assert users.size() == 1;
    }

    @Test
    @Order(5)
    void throwsExceptionWhenMultiTenantFilterIsNotPresentInAMultiTenantDocument() {
        var missingAnnotationEntity = new MissingAnnotationEntity();
        missingAnnotationEntityRepository.save(missingAnnotationEntity);
        assertThrows(RuntimeException.class, () -> missingAnnotationEntityRepository.findAll());
    }

    @Test
    @Order(6)
    void throwsExceptionWhenTenantDoesNotResolve() {
        tenants.set(null);
        assertThrows(RuntimeException.class, () -> repository.findAll());
    }
}
