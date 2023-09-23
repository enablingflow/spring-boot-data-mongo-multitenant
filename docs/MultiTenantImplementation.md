# MultiTenant Implementation

## Introduction

I implemented the MultiTenant support by:

1. Extending `SimpleMongoRepository` to add the missing EntityInformation JavaType to extract the `@MultiTenantField` value from the Entity.
2. Extending `MongoTemplate` to add the logic to add the `@MultiTenantField` value to the query and save operations.

## :warning: Unknown behaviour

I implemented those methods that we needed in our product. As we are using more functionality,
we detect the missing methods, and we add them to the library.

## Current implementation

### MultiTenantMongoRepository

| Method | Overriden | Description                                                                          |
| --- | --- |-----|
| `void deleteAll()` | :heavy_check_mark: | It missed the `entityInformation.getCollectionName()` in order to filter by tenant   |

### MultiTenantMongoTemplate

| Method | Overriden | Description |
| --- | --- |-------------|
| `<T> T doFindOne(String collectionName, CollectionPreparer<MongoCollection<Document>> collectionPreparer, Document query, Document fields, CursorPreparer preparer, Class<T> entityClass)` | :heavy_check_mark: | - |          |
