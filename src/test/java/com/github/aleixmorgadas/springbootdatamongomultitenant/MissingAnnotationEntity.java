package com.github.aleixmorgadas.springbootdatamongomultitenant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@MultiTenant
public class MissingAnnotationEntity {
    @Id
    ObjectId id;
}
