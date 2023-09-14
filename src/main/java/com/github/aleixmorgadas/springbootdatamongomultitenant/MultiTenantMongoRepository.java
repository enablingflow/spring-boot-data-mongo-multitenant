package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public class MultiTenantMongoRepository<T, ID> extends SimpleMongoRepository<T, ID> {
    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<T, ID> entityInformation;

    /**
     * Creates a new {@link SimpleMongoRepository} for the given {@link MongoEntityInformation} and {@link MongoTemplate}.
     *
     * @param metadata        must not be {@literal null}.
     * @param mongoOperations must not be {@literal null}.
     */
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
