package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@Testcontainers
public class MultiTenantTest {
    @ServiceConnection
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0.8");

    @Autowired
    private UserRepository repository;

    @BeforeEach
    void setup() {
        repository.save(new User(null, "tenantA"));
        repository.save(new User(null, "tenantA"));
        repository.save(new User(null, "tenantB"));
        repository.save(new User(null, "tenantC"));
    }

    @AfterEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void receivesOnlyTenantA() {
        var users = repository.findAll();
        assert users.size() == 2;
    }
}
