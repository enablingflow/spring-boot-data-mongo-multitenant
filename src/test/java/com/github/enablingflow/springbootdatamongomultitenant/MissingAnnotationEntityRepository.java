package com.github.enablingflow.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

public interface MissingAnnotationEntityRepository extends CrudRepository<MissingAnnotationEntity, ObjectId> {
}
