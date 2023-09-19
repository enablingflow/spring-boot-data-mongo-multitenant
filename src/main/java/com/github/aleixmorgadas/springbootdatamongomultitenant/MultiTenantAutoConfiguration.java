package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MultiTenantMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@AutoConfiguration
@ConditionalOnClass(MongoTemplate.class)
@EnableAutoConfiguration
@EnableMongoRepositories(repositoryBaseClass = MultiTenantMongoRepository.class)
public class MultiTenantAutoConfiguration {

    @Bean
    MultiTenantContext multiTenantContext() {
        return new MultiTenantContext();
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MultiTenantContext multiTenantContext) {
        return new MultiTenantMongoTemplate(mongoDbFactory, multiTenantContext);
    }
}
