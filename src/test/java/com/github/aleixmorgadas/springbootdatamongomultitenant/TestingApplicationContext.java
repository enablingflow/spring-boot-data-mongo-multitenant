package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MultiTenantMongoTemplate;

import java.util.Map;

@SpringBootApplication
public class TestingApplicationContext {

    public static void main(String[] args) {
        SpringApplication.run(TestingApplicationContext.class, args);
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
        return new MultiTenantMongoTemplate(mongoDbFactory, Map.of("default", () -> "tenantA"));
    }
}
