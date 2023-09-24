package org.springframework.data.mongodb.core;

import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenant;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantContext;
import com.github.aleixmorgadas.springbootdatamongomultitenant.MultiTenantField;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoWriter;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.lang.reflect.Field;
import java.util.List;

public class MultiTenantMongoTemplate extends MongoTemplate {
    private final MultiTenantContext<?> multiTenantContext;

    public MultiTenantMongoTemplate(MongoDatabaseFactory mongoDbFactory, MultiTenantContext<?> multiTenantContext) {
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
    protected <T> T doInsert(String collectionName, T objectToSave, MongoWriter<T> writer) {
        var entityClass = objectToSave.getClass();
        if (isMultiTenant(entityClass)) {
            if (multiTenantContext.isRoot()) {
                return super.doInsert(collectionName, objectToSave, writer);
            }
            var field = getMultiTenantField(entityClass);
            var tenantFilterValue = multiTenantContext.hasScopedTenant() ? multiTenantContext.getScopedTenant() : multiTenantContext.get();
            if (tenantFilterValue != null) {
                field.setAccessible(true);
                try {
                    if (field.getType().getName().equals("org.bson.types.ObjectId")) {
                        field.set(objectToSave, new ObjectId((String) tenantFilterValue));
                    } else {
                        field.set(objectToSave, tenantFilterValue);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            } else {
                throw new RuntimeException("Tenant filter value is null");
            }
        }
        return super.doInsert(collectionName, objectToSave, writer);
    }

    @Override
    protected <T> T doSave(String collectionName, T objectToSave, MongoWriter<T> writer) {
        var entityClass = objectToSave.getClass();
        if (isMultiTenant(entityClass)) {
            if (multiTenantContext.isRoot()) {
                return super.doSave(collectionName, objectToSave, writer);
            }
            var field = getMultiTenantField(entityClass);
            var tenantFilterValue = multiTenantContext.hasScopedTenant() ? multiTenantContext.getScopedTenant() : multiTenantContext.get();
            if (tenantFilterValue != null) {
                field.setAccessible(true);
                try {
                    field.set(objectToSave, tenantFilterValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Tenant filter value is null");
            }
        }
        return super.doSave(collectionName, objectToSave, writer);
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
        if (isMultiTenant(entityClass)) {
            if (multiTenantContext.isRoot()) {
                return multiTenantQuery;
            }
            var field = getMultiTenantField(entityClass);
            var tenantFilter = field.getAnnotation(MultiTenantField.class);
            var tenantFilterValue = multiTenantContext.hasScopedTenant() ? multiTenantContext.getScopedTenant() : multiTenantContext.get();
            if (tenantFilterValue != null) {
                if (field.getType() == ObjectId.class) {
                    multiTenantQuery.put(tenantFilter.value(), new ObjectId((String) tenantFilterValue));
                } else {
                    multiTenantQuery.put(tenantFilter.value(), tenantFilterValue);
                }
            } else {
                throw new RuntimeException("Tenant filter value is null");
            }
        }
        return multiTenantQuery;
    }

    private <S> Query multiTenantFilter(Query query, Class<S> entityClass) {
        if (isMultiTenant(entityClass)) {
            if (multiTenantContext.isRoot()) {
                return query;
            }
            var field = getMultiTenantField(entityClass);
            var tenantField = field.getAnnotation(MultiTenantField.class);
            var tenantFilterValue = multiTenantContext.hasScopedTenant() ? multiTenantContext.getScopedTenant() : multiTenantContext.get();
            if (tenantFilterValue != null) {
                if (field.getType() == ObjectId.class) {
                    query.addCriteria(Criteria.where(tenantField.value()).is(new ObjectId((String) tenantFilterValue)));
                } else {
                    query.addCriteria(Criteria.where(tenantField.value()).is(tenantFilterValue));
                }
            } else {
                throw new RuntimeException("Tenant filter value is null");
            }
        }
        return query;
    }

    private boolean isMultiTenant(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(MultiTenant.class);
    }

    private Field getMultiTenantField(Class<?> entityClass) {
        for (var field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(MultiTenantField.class)) {
                return field;
            }
        }
        throw new RuntimeException("Entity " + entityClass.getName() + " is annotated with @MultiTenant but no @MultiTenantField is present");
    }
}
