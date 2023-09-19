package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MultiTenantMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@TestConfiguration
@EnableMongoRepositories(repositoryBaseClass = MultiTenantMongoRepository.class)
public class MultiTenantTestConfig {
    @Bean
    public MultiTenantContext multiTenantContext() {
        MultiTenantContext multiTenantContext = new MultiTenantContext();
        multiTenantContext.set(() -> "tenantA");
        return multiTenantContext;
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MultiTenantContext multiTenantContext) {
        return new MultiTenantMongoTemplate(mongoDatabaseFactory, multiTenantContext);
    }
}
