# Specification

This software should hold articles, and the articles should contain an identification number, a name and available stock. It should be possible to load articles into the software from a file, see the attached inventory.json.
The warehouse software should also have products, products are made of different articles. Products should have a name, price and a list of articles of which they are made from with a quantity. The products should also be loaded from a file, see the attached products.json. 
 
The warehouse should have at least the following functionality;
* Get all products and quantity of each that is an available with the current inventory
* Remove(Sell) a product and update the inventory accordingly

# Design and technologies

* Inventory service is a gRPC service. Here the specification can be found:
inventory-api/src/main/proto/com/xxx/inventory/warehouse-inventory.proto
* Implementation uses minimum set of libraries to be as simple as possible
* Service uses PostgreSQL as main data storage
* jOOQ is used to specify RDBMS queries as compile-time-chacked DSL (the main reason it's here is that I wanted to avaluate this instrument)
* All data mutations are made in transactions. To achive data consistency pessimistic locking is used.
To avoid dead locks ordering is used (`SELECT ... FOR UPDATE OREDER BY ...`)
* Data loading methods apply articles and product changes in separate transactions. Client feed stream of changes for elements and service replies to response stream with a status of each element uploading.
See  `updateArticles` and `updateProducts` methods.
At the moment there is no a way to upload several articles/products in a single transaction.

Data loaders from the module `inventory-data-uploader` can not be considered as a complete implementation,
at the moment it's just a demonstration of the idea how uploading from data file can be implemented for the Inventory Service.

# Local run

1. Start PostgreSQL as docker-compose service

Run the following command from the root of the project:
```
docker-compose up -d
```

2. Set up required environment variables.

```
export DB_URL=jdbc:postgresql://127.0.0.1:5432/postgres
export DB_USER=postgres
export DB_PASSWORD=changeme
```

3. Apply Flyway DB migrations and generate Jooq queries data model

```
./gradlew :inventory-service:jooq
```

4. Now we ready to start the Inventory Service

```
./gradlew :inventory-service:run
```

# ToDo

There are a lot of things to do to make it production ready. Here is the most important of them:
1. add unit tests
2. add integration tests (run postgres using TestContainers)
3. use some common approach for application configuration
4. make uploaders more functional to make it possible to set up connection
5. if performance matters consider other persistence schema to avoid locking:
event based storage with Saga transactions.

# Tips

To play with service such tool as [BloomRPC](https://github.com/uw-labs/bloomrpc) can be used to fire requests.
Use the folloing proto file as service descriptor:

* inventory-api/src/main/proto/com/xxx/inventory/warehouse-inventory.proto