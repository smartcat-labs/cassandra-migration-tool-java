# cassandra-migration-tool-java

Cassandra migration tool for java is a small tool used to execute schema and data migration on cassandra cluster. Schema versioning state is saved in `schema_version` table, you can create schema and data migrations and they will be executed.

# Examples
In tests [MigrationEngineTest](https://github.com/smartcat-labs/cassandra-migration-tool-java/blob/master/src/test/java/com/smartcat/migration/MigrationEngineTest.java) can server as  example of one of use cases which this tools covers. There is already defined table in cassandra DB with production data in it and requirement is to add column and populate it.

Table is plane and simple (can be found in [init db.cql file](https://github.com/smartcat-labs/cassandra-migration-tool-java/blob/master/src/test/resources/db.cql)) and we use migration classes to do following:

1. Populate data initially with first `data` migration [InsertBooksMigration](https://github.com/smartcat-labs/cassandra-migration-tool-java/blob/master/src/test/java/com/smartcat/migration/migrations/data/InsertBooksMigration.java)
2. Add `genre` column with `schema` migration [AddBookGenreFieldMigration](https://github.com/smartcat-labs/cassandra-migration-tool-java/blob/master/src/test/java/com/smartcat/migration/migrations/schema/AddBookGenreFieldMigration.java)
3. Populate `genre` column with second `data` migration [AddGenreMigration](https://github.com/smartcat-labs/cassandra-migration-tool-java/blob/master/src/test/java/com/smartcat/migration/migrations/data/AddGenreMigration.java)

# Future work
1. Add possible to assign listener which will get notified when schema is in agreement (this [fix](https://datastax-oss.atlassian.net/browse/JAVA-669) makes it possible )
