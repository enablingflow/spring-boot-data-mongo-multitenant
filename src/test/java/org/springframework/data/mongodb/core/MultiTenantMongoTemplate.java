package org.springframework.data.mongodb.core;

import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenant;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantField;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiTenantMongoTemplate extends MongoTemplate {
    private final Map<String, Supplier<String>> multiTenantFilter;

    public MultiTenantMongoTemplate(MongoDatabaseFactory mongoDbFactory, Map<String, Supplier<String>> multiTenantFilter) {
        super(mongoDbFactory);
        this.multiTenantFilter = multiTenantFilter;
    }

    @Override
    protected <T> T doFindOne(String collectionName, CollectionPreparer<MongoCollection<Document>> collectionPreparer, Document query, Document fields, CursorPreparer preparer, Class<T> entityClass) {
        var multiTenantQuery = multiTenantFilter(query, entityClass);
        return super.doFindOne(collectionName, collectionPreparer, multiTenantQuery, fields, preparer, entityClass);
    }

    @Override
    protected <T> List<T> doFind(String collectionName, CollectionPreparer<MongoCollection<Document>> collectionPreparer, Document query, Document fields, Class<T> entityClass, CursorPreparer preparer) {
        var multiTenantQuery = multiTenantFilter(query, entityClass);
        return super.doFind(collectionName, collectionPreparer, multiTenantQuery, fields, entityClass, preparer);
    }

    @Override
    protected <S, T> List<T> doFind(CollectionPreparer<MongoCollection<Document>> collectionPreparer, String collectionName,
                                    Document query, Document fields, Class<S> sourceClass, Class<T> targetClass, CursorPreparer preparer) {
        var multiTenantQuery = multiTenantFilter(query, sourceClass);
        return super.doFind(collectionPreparer, collectionName, multiTenantQuery, fields, sourceClass, targetClass, preparer);
    }

    @Override
    protected <T> T doFindAndRemove(CollectionPreparer collectionPreparer, String collectionName, Document query, Document fields, Document sort, Collation collation, Class<T> entityClass) {
        var multiTenantQuery = multiTenantFilter(query, entityClass);
        return super.doFindAndRemove(collectionPreparer, collectionName, multiTenantQuery, fields, sort, collation, entityClass);
    }

    @Override
    protected <T> T doFindAndModify(CollectionPreparer collectionPreparer, String collectionName, Document query, Document fields, Document sort, Class<T> entityClass, UpdateDefinition update, FindAndModifyOptions options) {
        var multiTenantQuery = multiTenantFilter(query, entityClass);
        return super.doFindAndModify(collectionPreparer, collectionName, multiTenantQuery, fields, sort, entityClass, update, options);
    }

    @Override
    protected <T> T doFindAndReplace(CollectionPreparer collectionPreparer, String collectionName, Document mappedQuery, Document mappedFields, Document mappedSort, com.mongodb.client.model.Collation collation, Class<?> entityType, Document replacement, FindAndReplaceOptions options, Class<T> resultType) {
        var multiTenantQuery = multiTenantFilter(mappedQuery, entityType);
        return super.doFindAndReplace(collectionPreparer, collectionName, multiTenantQuery, mappedFields, mappedSort, collation, entityType, replacement, options, resultType);
    }

    private <S> Document multiTenantFilter(Document query, Class<S> entityClass) {
        var multiTenantQuery = query;
        if ((query.getClass().getName().equals("org.springframework.data.mongodb.util.EmptyDocument"))) {
            multiTenantQuery = new Document();
        }
        if (entityClass.isAnnotationPresent(MultiTenant.class)) {
            for (var field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(MultiTenantField.class)) {
                    var tenantFilter = field.getAnnotation(MultiTenantField.class);
                    var tenantFilterValue = multiTenantFilter.get(tenantFilter.mapper()).get();
                    if (tenantFilterValue != null) {
                        if (field.getType() == ObjectId.class) {
                            multiTenantQuery.put(tenantFilter.value(), new ObjectId(tenantFilterValue));
                        } else {
                            multiTenantQuery.put(tenantFilter.value(), tenantFilterValue);
                        }
                    }
                }
            }
        }
        return multiTenantQuery;
    }
}
