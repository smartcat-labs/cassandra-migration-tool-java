package io.smartcat.migration;

import static org.junit.Assert.assertEquals;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import io.smartcat.migration.migrations.data.AddGenreMigration;
import io.smartcat.migration.migrations.data.InsertBooksMigration;
import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;

public class MigrationEngineBooksTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineBooksTest.class);

    private static final String LOCAL_DATACENTER = "DC1";
    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test_books";
    private static final String CQL = "books.cql";

    private static CqlSession session;

    @BeforeClass
    public static void init() throws Exception {
        LOGGER.info("Starting embedded cassandra server");
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml");

        session = new CqlSessionBuilder()
                .withLocalDatacenter(LOCAL_DATACENTER)
                .addContactPoint(new InetSocketAddress(CONTACT_POINT, PORT))
                .build();

        LOGGER.info("Initialize keyspace");
        new CQLDataLoader(session).load(new ClassPathCQLDataSet(CQL, false, true, KEYSPACE));
    }

    @AfterClass
    public static void tearDown() {
        session.close();
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

        final SimpleStatement select = QueryBuilder.selectFrom("books").all().build();
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
