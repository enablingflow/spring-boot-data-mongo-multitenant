package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public class MultiTenantMongoRepository<T, ID> extends SimpleMongoRepository<T, ID> {
    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<T, ID> entityInformation;

    public MultiTenantMongoRepository(MongoEntityInformation metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    @Override
    public void deleteAll() {
        mongoOperations.remove(new Query(), entityInformation.getJavaType(), entityInformation.getCollectionName());
    }
}
