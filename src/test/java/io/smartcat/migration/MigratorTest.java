package io.smartcat.migration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.smartcat.migration.MigrationEngine.Migrator;
import io.smartcat.migration.migrations.schema.AddBookGenreFieldMigration;
import io.smartcat.migration.migrations.schema.AddBookISBNFieldMigration;

public class MigratorTest extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MigratorTest.class);

    private static final String CONTACT_POINT = "localhost";
    private static final int PORT = 9142;
    private static final String KEYSPACE = "migration_test_books";
    private static final String CQL = "books.cql";

    private static Session session;
    private static Cluster cluster;

    private CassandraVersioner versioner;
    private Migrator migrator;

    private CassandraMetadataAnalyzer metadataAnalyzer;

    @BeforeClass
    public static void init() throws Exception {
        LOGGER.info("Starting embedded cassandra server");
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("another-cassandra.yaml");

        LOGGER.info("Connect to embedded db");
        cluster = Cluster.builder().addContactPoints(CONTACT_POINT).withPort(PORT).build();
        session = cluster.connect();
    }

    @Before
    public void setUp() throws Exception {
        LOGGER.info("Initialize keyspace");
        final CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        cqlDataLoader.load(new ClassPathCQLDataSet(CQL, false, true, KEYSPACE));

        versioner = new CassandraVersioner();
        migrator = MigrationEngine.withSession(session);
        metadataAnalyzer = new CassandraMetadataAnalyzer(session);
    }

    @AfterClass
    public static void tearDown() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    @Test
    public void executeOneMigration() throws Exception {
        assertTableDoesntContainsColumns("books", "genre");

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(1));

        migrator.migrate(resources);

        assertTableContainsColumns("books", "genre");
    }

    @Test
    public void executeTwoMigrations() throws Exception {
        assertTableDoesntContainsColumns("books", "genre", "isbn");

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(1));
        resources.addMigration(new AddBookISBNFieldMigration(2));

        migrator.migrate(resources);

        assertTableContainsColumns("books", "genre", "isbn");
    }

    @Test
    public void updateVersionAfterMigration() throws Exception {
        int versionBeforeMigration = getCurrentVersion();

        Migration migration = new AddBookGenreFieldMigration(1);
        final MigrationResources resources = new MigrationResources();
        resources.addMigration(migration);

        migrator.migrate(resources);

        // verify
        assertThat(getCurrentVersion(), not(is(versionBeforeMigration)));
        assertThat(getCurrentVersion(), is(migration.getVersion()));
    }

    @Test
    public void skipMigrationWithVersionOlderThanCurrentSchemaVersion() throws Exception {
        Migration migrationWithNewerVersion = new AddBookGenreFieldMigration(2);
        Migration migrationWithOlderVersion = new AddBookISBNFieldMigration(1);

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(migrationWithNewerVersion);
        resources.addMigration(migrationWithOlderVersion);

        migrator.migrate(resources);

        // verify
        assertThat(getCurrentVersion(), is(migrationWithNewerVersion.getVersion()));
        assertTableDoesntContainsColumns("books", "isbn");
    }

    @Test
    public void skipMigrationWithSameVersionThanCurrentSchemaVersion() throws Exception {
        int versionBeforeMigration = getCurrentVersion();

        final MigrationResources resources = new MigrationResources();
        resources.addMigration(new AddBookGenreFieldMigration(versionBeforeMigration));

        migrator.migrate(resources);

        // verify
        assertThat(getCurrentVersion(), is(versionBeforeMigration));
    }

    private int getCurrentVersion() {
        return versioner.getCurrentVersion(session, MigrationType.SCHEMA);
    }

    private void assertTableDoesntContainsColumns(String table, String... columns) {
        for (String column : columns) {
            assertFalse(metadataAnalyzer.columnExistInTable(column, table));
        }
    }

    private void assertTableContainsColumns(String table, String... columns) {
        for (String column : columns) {
            assertTrue(metadataAnalyzer.columnExistInTable(column, table));
        }
    }

}
