package io.smartcat.migration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;
import io.smartcat.migration.migrations.schema.AddBookISBNFieldMigration;
import io.smartcat.migration.migrations.schema.CreateItemByNumberAndExternalIdMigration;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author dalvizu
 */
public class MigrationTestingOrderingTest extends BaseTest {
    private static Session session;

    private static Cluster cluster;

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationEngineBooksTest.class);

    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test_books";
    private static final String CQL = "books.cql";

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
    public void noUpdatesToApply() {
        CassandraVersioner versioner = new CassandraVersioner();
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(1));

        boolean result = MigrationEngine.withSession(session).migrate(resources);
        assertEquals(true, result);
        assertEquals(Integer.valueOf(1), versioner.getMostRecentUpdateApplied(session, MigrationType.SCHEMA));

        result = MigrationEngine.withSession(session).migrate(resources);
        assertEquals(true, result);
        assertEquals(Integer.valueOf(1), versioner.getMostRecentUpdateApplied(session, MigrationType.SCHEMA));
    }

    @Test
    public void testUpdatesArriveOutOfOrder() {

        CassandraVersioner cassandraVersioner = new CassandraVersioner();
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(1));
        resources.addMigration(new AddBookISBNFieldMigration(3));

        boolean result = MigrationEngine.withSession(session).migrate(resources);
        assertEquals(true, result);
        assertThat(cassandraVersioner.getUpdatesApplied(session, MigrationType.SCHEMA), contains(1, 3));
        assertEquals(3, cassandraVersioner.getCurrentVersion(session, MigrationType.SCHEMA));

        resources.addMigration(new CreateItemByNumberAndExternalIdMigration(2));
        result = MigrationEngine.withSession(session).migrate(resources);
        assertEquals(true, result);
        assertEquals(3, cassandraVersioner.getCurrentVersion(session, MigrationType.SCHEMA));
        assertThat(cassandraVersioner.getUpdatesApplied(session, MigrationType.SCHEMA), contains(1, 2, 3));
    }
}
