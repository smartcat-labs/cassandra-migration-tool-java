package io.smartcat.migration;

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import io.smartcat.migration.migrations.data.AddGenreMigration;
import io.smartcat.migration.migrations.data.InsertBooksMigration;
import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;

public class MigrationEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineTest.class);

    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test";
    private static final String CQL = "db.cql";

    private static Session session;
    private static Cluster cluster;

    @BeforeClass
    public static void init() throws Exception {
        LOGGER.info("Starting embedded cassandra server");
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml");

        LOGGER.info("Connect to embedded db");
        cluster = Cluster.builder().addContactPoints(CONTACT_POINT).withPort(PORT).build();
        session = cluster.connect();

        LOGGER.info("Initialize keyspace");
        final CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        cqlDataLoader.load(new ClassPathCQLDataSet(CQL, false, true, KEYSPACE));
    }

    @AfterClass
    public static void tearDown() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    @Test
    public void test_schema_migration() {
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(1));
        final boolean result = MigrationEngine.withSession(session).migrate(resources);

        assertEquals(true, result);
    }

    @Test
    public void test_data_migration() {
        final MigrationResources initialResources = new MigrationResources();
        initialResources.addMigration(new InsertBooksMigration(1));
        MigrationEngine.withSession(session).migrate(initialResources);

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(1));
        resources.addMigration(new AddGenreMigration(2));
        MigrationEngine.withSession(session).migrate(resources);

        final Statement select = QueryBuilder.select().all().from("books");
        final ResultSet results = session.execute(select);

        for (final Row row : results) {
            final String genre = row.getString("genre");
            final String name = row.getString("name");

            if (name.equals("Journey to the Center of the Earth")) {
                assertEquals("fantasy", genre);
            } else if (name.equals("Fifty Shades of Grey")) {
                assertEquals("erotica", genre);
            } else {
                assertEquals("programming", genre);
            }
        }
    }

}
