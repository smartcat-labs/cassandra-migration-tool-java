# cassandra-migration-tool-java
[![Build Status](https://travis-ci.org/smartcat-labs/cassandra-migration-tool-java.svg?branch=develop)](https://travis-ci.org/smartcat-labs/cassandra-migration-tool-java)

Cassandra migration tool for java is a lightweight tool used to execute schema and data migration on Cassandra database. Schema versioning state is saved in `schema_version` table which stores name and description of migration along with type and timestamp. 

There are two types of migrations:
1. SCHEMA migrations, which alter database schema (add or remove column, change type of column, add table, etc)
2. DATA migrations, which alter data (update, read from one table to another, calculate new fields based on existing data, etc).

# Why we did it

The idea behind this project was born while working on a project with live data. Since the development required changes to the data model we had to figure out a way we can update the database schema while keeping the data. We ended up with a lightweight tool that versions database and enables us to change schema and transform data when and how we want (as a part of a build and deploy cycle or application itself). Since these changes are part of the codebase we could also test them before executing on a live cluster.


# Versioning

We are going to follow the official DataStax Java driver versions and build our driver for each major, minor and patch version of the driver. We chose this path because of the simplicity to develop, test and deploy for different versions of the driver.
This means that if you are using the driver version 2.1.9 you can add this dependency to your project:
```xml
<dependency>
    <groupId>io.smartcat</groupId>
    <artifactId>cassandra-migration-tool</artifactId>
    <version>2.1.9.0</version>
</dependency>
```
or whatever the latest build version of the migration tool is. Check the version at the [Maven repository](http://mvnrepository.com/artifact/io.smartcat/cassandra-migration-tool).

# Examples
In tests [MigrationEngineTest](src/test/java/io/smartcat/migration/MigrationEngineTest.java) can serve as an example of one use case which this tool can cover. There is already defined table in cassandra DB with production data in it and requirement is to add column and populate it with data.

The initial table is simple (can be found in [init db.cql file](src/test/resources/db.cql)) and we use migration classes to do the following:

1. Populate data initially with first `data` migration [InsertBooksMigration](src/test/java/io/smartcat/migration/migrations/data/InsertBooksMigration.java)
2. Add `genre` column with `schema` migration [AddBookGenreFieldMigration](src/test/java/io/smartcat/migration/migrations/schema/AddBookGenreFieldMigration.java)
3. Populate `genre` column with second `data` migration [AddGenreMigration](src/test/java/io/smartcat/migration/migrations/data/AddGenreMigration.java)

# Schema agreement
When executing schema migrations it is necessary to wait for cluster to propagate schema on all nodes. Schema agreement is implemented based on this [fix](https://datastax-oss.atlassian.net/browse/JAVA-669) and is exposed through [Migration](src/main/java/io/smartcat/migration/Migration.java) abstract class.
To execute a statement with schema agreement you can use `executeWithSchemaAgreement` method.
