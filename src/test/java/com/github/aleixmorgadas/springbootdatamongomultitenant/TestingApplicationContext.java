package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MultiTenantMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SpringBootApplication
@EnableMongoRepositories(repositoryBaseClass = MultiTenantMongoRepository.class)
public class TestingApplicationContext {

    public static void main(String[] args) {
        SpringApplication.run(TestingApplicationContext.class, args);
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, Map<String, Supplier<String>> tenants) {
        return new MultiTenantMongoTemplate(mongoDbFactory, tenants);
    }

    @Bean
    Map<String, Supplier<String>> tenants() {
        HashMap<String, Supplier<String>> tenants = new HashMap<>();
        tenants.put("default", () -> "tenantA");
        return tenants;
    }
}
