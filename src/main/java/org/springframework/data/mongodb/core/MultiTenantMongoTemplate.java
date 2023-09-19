package org.springframework.data.mongodb.core;

import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenant;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantContext;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantField;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.util.List;

public class MultiTenantMongoTemplate extends MongoTemplate {
    private final MultiTenantContext multiTenantContext;

    public MultiTenantMongoTemplate(MongoDatabaseFactory mongoDbFactory, MultiTenantContext multiTenantContext) {
        super(mongoDbFactory);
        this.multiTenantContext = multiTenantContext;
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

    @Override
    protected <T> DeleteResult doRemove(String collectionName, Query query, Class<T> entityClass, boolean multi) {
        var multiTenantQuery = multiTenantFilter(query, entityClass);
        return super.doRemove(collectionName, multiTenantQuery, entityClass, multi);
    }

    private <S> Document multiTenantFilter(Document document, Class<S> entityClass) {
        var multiTenantQuery = document;
        if ((document.getClass().getName().equals("org.springframework.data.mongodb.util.EmptyDocument"))) {
            multiTenantQuery = new Document();
        }
        if (entityClass.isAnnotationPresent(MultiTenant.class)) {
            if (multiTenantContext.isRoot()) {
                return multiTenantQuery;
            }
            var hasMultiTenantFilter = false;
            for (var field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(MultiTenantField.class)) {
                    hasMultiTenantFilter = true;
                    var tenantFilter = field.getAnnotation(MultiTenantField.class);
                    var tenantFilterValue = multiTenantContext.hasScopedTenant() ? multiTenantContext.getScopedTenant() : multiTenantContext.get();
                    if (tenantFilterValue != null) {
                        if (field.getType() == ObjectId.class) {
                            multiTenantQuery.put(tenantFilter.value(), new ObjectId(tenantFilterValue));
                        } else {
                            multiTenantQuery.put(tenantFilter.value(), tenantFilterValue);
                        }
                    } else {
                        throw new RuntimeException("Tenant filter value is null");
                    }
                }
            }
            if (!hasMultiTenantFilter) {
                throw new RuntimeException("Entity " + entityClass.getName() + " is annotated with @MultiTenant but no @MultiTenantField is present");
            }
        }
        return multiTenantQuery;
    }

    private <S> Query multiTenantFilter(Query query, Class<S> entityClass) {
        if (entityClass.isAnnotationPresent(MultiTenant.class)) {
            if (multiTenantContext.isRoot()) {
                return query;
            }
            var hasMultiTenantFilter = false;
            for (var field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(MultiTenantField.class)) {
                    hasMultiTenantFilter = true;
                    var tenantField = field.getAnnotation(MultiTenantField.class);
                    var tenantFilterValue = multiTenantContext.hasScopedTenant() ? multiTenantContext.getScopedTenant() : multiTenantContext.get();
                    if (tenantFilterValue != null) {
                        if (field.getType() == ObjectId.class) {
                            query.addCriteria(Criteria.where(tenantField.value()).is(new ObjectId(tenantFilterValue)));
                        } else {
                            query.addCriteria(Criteria.where(tenantField.value()).is(tenantFilterValue));
                        }
                    } else {
                        throw new RuntimeException("Tenant filter value is null");
                    }
                }
            }
            if (!hasMultiTenantFilter) {
                throw new RuntimeException("Entity " + entityClass.getName() + " is annotated with @MultiTenant but no @MultiTenantField is present");
            }
        }
        return query;
    }
}
