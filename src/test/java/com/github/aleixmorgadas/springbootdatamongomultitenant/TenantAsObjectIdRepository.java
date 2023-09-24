package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TenantAsObjectIdRepository extends MongoRepository<TenantAsObjectId, ObjectId> {
}
