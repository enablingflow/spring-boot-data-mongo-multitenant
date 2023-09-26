package com.github.enablingflow.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
    private TenantAsObjectIdRepository tenantAsObjectIdRepository;

    @Autowired
    private WithNullableTenantRepository withNullableTenantRepository;

    @Autowired
    private MissingAnnotationEntityRepository missingAnnotationEntityRepository;

    @Autowired
    private MultiTenantContext<String> tenants;

    @BeforeEach
    void setup() throws Exception {
        tenants.performAsRoot(() -> {
            repository.save(new User(null, "Alex", "tenantA"));
            repository.save(new User(null, "Anna", "tenantA"));
            repository.save(new User(null, "Bart", "tenantB"));
            repository.save(new User(null, "Clementine", "tenantC"));
        });
    }

    @AfterEach
    void clean() throws Exception {
        tenants.performAsRoot(() -> repository.deleteAll());
    }

    @Test
    @Order(1)
    void receivesOnlyTenantA() {
        tenants.set("tenantA");
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
        tenants.set("tenantB");
        var users = repository.findAll();
        assert users.size() == 1;
    }

    @Test
    @Order(5)
    void throwsExceptionWhenMultiTenantFilterIsNotPresentInAMultiTenantDocument() {
        var missingAnnotationEntity = new MissingAnnotationEntity();
        assertThrows(RuntimeException.class, () -> missingAnnotationEntityRepository.save(missingAnnotationEntity));
        assertThrows(RuntimeException.class, () -> missingAnnotationEntityRepository.findAll());
    }

    @Test
    @Order(6)
    void throwsExceptionWhenTenantDoesNotResolve() {
        tenants.set(null);
        assertThrows(RuntimeException.class, () -> repository.findAll());
    }

    @Test
    @Order(7)
    void modifyUser() throws Exception {
        var userByTenantA = tenants.performAsTenant("tenantA", () -> {
            var user = repository.findAll().stream().findFirst().get();
            user.setName("tenantA-modified");
            return repository.save(user);
        });
        assertEquals("tenantA-modified", userByTenantA.name);
    }

    @Test
    @Order(8)
    void setTenantOnSave() throws Exception {
        tenants.performAsTenant("tenantA", () -> {
            var user = new User(null, "name", null);
            var savedUser = repository.save(user);
            assertEquals("tenantA", savedUser.tenantId);
        });
    }

    @Test
    @Order(9)
    void setTenantWhichItIsAnObjectId() throws Exception {
        tenants.performAsTenant("507f1f77bcf86cd799439011", () -> {
            var user = new TenantAsObjectId(null, null);
            var savedUser = tenantAsObjectIdRepository.save(user);
            assertEquals("507f1f77bcf86cd799439011", savedUser.tenantId.toString());
        });
    }

    @Test
    @Order(10)
    void withoutTenant() throws Exception {
        var user = new WithNullableTenant(null, null);
        var savedUser = withNullableTenantRepository.save(user);

        assertNotNull(savedUser.id);
        assertNull(savedUser.tenant);
    }

    @Test
    @Order(11)
    void itShouldNotOverwriteTenant() throws Exception {
        var user = tenants.performAsTenant("tenantA", () -> repository.save(new User(null, "aleix", null)));
        assertThat(user.id).isNotNull();
        assertThat(user.name).isEqualTo("aleix");
        assertThat(user.tenantId).isEqualTo("tenantA");
        user.setName("Aleix");
        assertThrows(RuntimeException.class, () -> tenants.performAsTenant("tenantB", () -> repository.save(user)));
    }

    @Test
    @Order(12)
    void itShouldThrowExceptionWhenTenantIsNotTheSameAsStored() throws Exception {
        var savedUser = tenants.performAsTenant("507f1f77bcf86cd799439011", () -> {
            var user = new TenantAsObjectId(null, null);
            return tenantAsObjectIdRepository.save(user);
        });
        assertEquals("507f1f77bcf86cd799439011", savedUser.tenantId.toString());
        assertThrows(RuntimeException.class, () -> {
            tenants.performAsTenant(
                    "666f1f77bcf86cd799439777",
                    () -> tenantAsObjectIdRepository.save(savedUser));
        });
    }

    @Test
    @Order(13)
    void itAllowsToStoreOtherTenantDocsWhenRoot() throws Exception {
        var savedUser = tenants.performAsRoot(() -> {
            var user = new TenantAsObjectId(null, new ObjectId("507f1f77bcf86cd799439011"));
            return tenantAsObjectIdRepository.save(user);
        });
        assertEquals("507f1f77bcf86cd799439011", savedUser.tenantId.toString());
    }

    @Test
    @Order(14)
    void itDoesNotLoseTheContextWhenNestingRootExecutions() throws Exception {
        tenants.performAsRoot(() -> {
            assertThat(tenants.isRoot()).isTrue();
            tenants.performAsRoot(() -> {
                assertThat(tenants.isRoot()).isTrue();
            });
            assertThat(tenants.isRoot()).isTrue();
        });
    }

    @Test
    @Order(15)
    void itDoesNotLoseTheContextWhenNestingAsTenantExecutions() throws Exception {
        tenants.performAsTenant("tenantA", () -> {
            assertThat(tenants.getScopedTenant()).isEqualTo("tenantA");
            tenants.performAsTenant("tenantA", () -> {
                assertThat(tenants.getScopedTenant()).isEqualTo("tenantA");
            });
            assertThat(tenants.getScopedTenant()).isEqualTo("tenantA");
        });
    }
}
